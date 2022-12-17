package homework03.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import homework03.model.CommentsSnapshot
import homework03.model.ChildrenPostData
import homework03.model.Comment
import homework03.model.SubComment
import homework03.model.TopicDetailsData
import homework03.model.TopicSnapshot
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get

class RedditClient {
    private val client = HttpClient(CIO)
    private val objectMapper = ObjectMapper()

    suspend fun getTopic(name: String): TopicSnapshot {
        val aboutJson: String = client.get("https://www.reddit.com/r/$name/about/.json").body()
        val postsJson: String = client.get("https://www.reddit.com/r/$name/.json").body()

        val topicMainData = objectMapper.readValue(aboutJson, TopicDetailsData::class.java)
        val posts = objectMapper.readValue(postsJson, ChildrenPostData::class.java)
        return TopicSnapshot(
            creationTime = topicMainData.data.created,
            subscribersOnline = topicMainData.data.activeUserCount,
            description = topicMainData.data.publicDescription,
            posts = posts.data.children.map { it.data }
        )
    }

    val commentsList: MutableList<Comment> = ArrayList()

    suspend fun getComments(name: String, title: String): CommentsSnapshot {
        val json: String = client.get("https://www.reddit.com/r/$name/comments/$title/.json").body()
        val commentsTree: JsonNode = objectMapper.readTree(json)
        return CommentsSnapshot(commentsTree[1]["data"]["children"].map {
            val data = it["data"]
            val parsedData: SubComment =
                objectMapper.treeToValue(data, SubComment::class.java)
            Comment(
                parsedData.created,
                parsedData.ups,
                parsedData.downs,
                parsedData.body,
                parsedData.author,
                name,
                emptyList(),
                parsedData.depth,
                parsedData.id
            )
        })
    }
}