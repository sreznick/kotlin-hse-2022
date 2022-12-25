package homework03

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

class RedditClient {

    private val httpClient = HttpClient(CIO)

    private val objectMapper = ObjectMapper()

    suspend fun getTopic(name: String): TopicSnapshot {
        val jsonAbout: String = httpClient.get("https://www.reddit.com/r/$name/about/.json").body()
        val jsonPosts: String = httpClient.get("https://www.reddit.com/r/$name/.json").body()

        val topicData = objectMapper.readValue(jsonAbout, TopicDetailsData::class.java)
        val topicPosts = objectMapper.readValue(jsonPosts, ChildrenPostData::class.java)

        return TopicSnapshot(
            creationTime = topicData.data.created,
            online = topicData.data.activeUserCount,
            description = topicData.data.publicDescription,
            posts = topicPosts.data.children.map { it.data }
        )
    }

    val commentsList: MutableList<Comment> = ArrayList()

    suspend fun getComments(name: String, title: String): CommentsSnapshot {
        val json: String = httpClient.get("https://www.reddit.com/r/$name/comments/$title/.json").body()
        val tree: JsonNode = objectMapper.readTree(json)

        return CommentsSnapshot(tree[1]["data"]["children"].map {
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