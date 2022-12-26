package homework03.redditClient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.engine.cio.CIO
import com.fasterxml.jackson.databind.ObjectMapper
import homework03.models.Comment
import homework03.models.CommentsSnapshot
import homework03.networkingDTO.comments.SubCommentDTO
import homework03.models.TopicSnapshot
import homework03.networkingDTO.topic.ChildrenPost
import homework03.networkingDTO.topic.TopicDetails
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

internal class RedditClient {
    private val client = HttpClient(CIO)
    private val objectMapper = ObjectMapper()
    private val domainName = "https://www.reddit.com/r"

    suspend fun getTopic(name: String): TopicSnapshot {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val aboutBodyJSON: String = client.get("$domainName/$name/about/.json").body()
        val postsBodyJSON: String = client.get("$domainName/$name/.json").body()

        val topicMainData = objectMapper.readValue(aboutBodyJSON, TopicDetails::class.java)
        val posts = objectMapper.readValue(postsBodyJSON, ChildrenPost::class.java)
        return TopicSnapshot(
                creationTime = topicMainData.data.created,
                subscribersOnline = topicMainData.data.activeUserCount,
                description = topicMainData.data.publicDescription,
                posts = posts.data.children.map { it.data }
        )
    }

    suspend fun getComments(name: String, title: String): CommentsSnapshot {
        val commentsBodyJSON: String = client.get("$domainName/$name/comments/$title/.json").body()
        val commentsTree: JsonNode = objectMapper.readTree(commentsBodyJSON)

        return CommentsSnapshot(commentsTree[1]["data"]["children"].map {
            val data = it["data"]
            val commentDTO: SubCommentDTO = objectMapper.treeToValue(data, SubCommentDTO::class.java)
            Comment(
                    created = commentDTO.created,
                    ups = commentDTO.ups,
                    downs = commentDTO.downs,
                    body = commentDTO.body,
                    author = commentDTO.author,
                    replyTo = name,
                    replies = emptyList(),
                    depth = commentDTO.depth,
                    id = commentDTO.id
            )
        })
    }
}