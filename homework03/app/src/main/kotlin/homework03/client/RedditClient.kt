package homework03.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import homework03.json.comment.Comment
import homework03.json.comment.CommentsSnapshot
import homework03.json.topic.PostChildData
import homework03.json.topic.TopicInfoData
import homework03.json.topic.TopicSnapshot
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

object  RedditClient {
    private val httpClient = HttpClient(CIO)
    private val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    suspend fun getTopic(name: String): TopicSnapshot {
        val jsonAbout: String = httpClient.get("https://www.reddit.com/r/$name/about/.json").body()
        val jsonPosts: String = httpClient.get("https://www.reddit.com/r/$name/.json").body()
        val topicData = objectMapper.readValue(jsonAbout, TopicInfoData::class.java)
        val topicPosts = objectMapper.readValue(jsonPosts, PostChildData::class.java)
        return TopicSnapshot(
            creationTime = topicData.data.created,
            online = topicData.data.activeUserCount,
            description = topicData.data.publicDescription,
            posts = topicPosts.data.children.map { it.data }
        )
    }

    suspend fun getComments(topicName: String, title: String): CommentsSnapshot {
        val json: String = httpClient.get("https://www.reddit.com/r/$topicName/comments/$title/.json").body()
        val tree: JsonNode = objectMapper.readTree(json)
        var commentsId = 0L

        fun deserializeComment(commentJsonNode: JsonNode, parentId: Long?, depth: Int): Comment {
            val curId = commentsId++
            val child: MutableList<Comment> = arrayListOf()
            try {
                for (ch in commentJsonNode["data"]["replies"]["data"]["children"]) {
                    child.add(deserializeComment(ch, curId, depth + 1))
                }
            } catch (_: NullPointerException) {}

            return Comment(
                created = commentJsonNode["data"]["created"].asDouble(),
                ups = commentJsonNode["data"]["ups"].asLong(),
                downs = commentJsonNode["data"]["downs"].asLong(),
                body = commentJsonNode["data"]["body"].toPrettyString(),
                author = commentJsonNode["data"]["author_fullname"].toPrettyString(),
                replyTo = parentId,
                replies = child,
                depth = depth,
                id = curId
            )
        }
        val comments: MutableList<Comment> = arrayListOf()
        for (comment in tree[1]["data"]["children"]) {
            comments.add(deserializeComment(comment, null, 0))
        }
        return CommentsSnapshot(comments)
    }
}