import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class RedditClient {
    suspend fun getTopic(name: String): TopicSnapshot {
        val client = HttpClient()
        var aboutTopicResponse = ""
        var mainPageResponse = ""
        coroutineScope {
            launch { aboutTopicResponse = client.get("https://www.reddit.com/r/${name}/about/.json").body() }
            launch { mainPageResponse = client.get("https://www.reddit.com/r/${name}/.json").body() }
        }
        client.close()

        val mapper = jacksonObjectMapper()
        val aboutTopicNode = mapper.readTree(aboutTopicResponse)
        val mainPageNode = mapper.readTree(mainPageResponse)

        val discussionList = mainPageNode.get("data").get("children")
        val discussions = ArrayList<Discussion>()
        for (node in discussionList) {
            discussions.add(
                Discussion(
                    id = node.get("data").get("id").asText(),
                    author = node.get("data").get("author").asText(),
                    publicationTime = node.get("data").get("created").asInt(),
                    ups = node.get("data").get("ups").asInt(),
                    downs = node.get("data").get("downs").asInt(),
                    title = node.get("data").get("title").asText(),
                    text = node.get("data").get("selftext").asText(),
                    htmlText = node.get("data").get("selftext_html").asText()
                )
            )
        }
        return TopicSnapshot(
            creationTime = aboutTopicNode.get("data").get("created").asInt(),
            onlineUsers = aboutTopicNode.get("data").get("active_user_count").asInt(),
            description = aboutTopicNode.get("data").get("public_description").asText(),
            discussions = discussions,
            receivingTime = System.currentTimeMillis()
        )
    }

    suspend fun getComments(topic: String, discussionId: String): CommentsSnapshot {
        val client = HttpClient()
        val response: String = client.get("https://www.reddit.com/r/${topic}/comments/${discussionId}/.json").body()
        client.close()

        val mapper = jacksonObjectMapper()
        val commentsNode = mapper.readTree(response)

        val comments = getCommentsList(commentsNode.get(1).get("data").get("children"), discussionId)
        val linearComments = getLinearCommentsList(comments)
        return CommentsSnapshot(System.currentTimeMillis(), comments, linearComments)
    }

    private fun getCommentsList(commentsNode: JsonNode?, discussionId: String, depth: Int = 1): List<Comment> {
        val comments = ArrayList<Comment>()
        if (commentsNode != null) {
            for (node in commentsNode) {
                comments.add(
                    Comment(
                        id = node.get("data").get("id").asText(),
                        replyTo = node.get("data").get("parent_id").asText(),
                        depth = depth,
                        discussionId = discussionId,
                        publicationTime = node.get("data").get("created")?.asInt(),
                        ups = node.get("data").get("ups")?.asInt(),
                        downs = node.get("data").get("downs")?.asInt(),
                        text = node.get("data").get("body")?.asText(),
                        author = node.get("data").get("author")?.asText(),
                        replies = getCommentsList(
                            node.get("data").get("replies")?.get("data")?.get("children"),
                            discussionId,
                            depth + 1
                        )
                    )
                )
            }
        }
        return comments
    }

    private fun getLinearCommentsList(comments: List<Comment>): List<Comment> {
        val linearComments = ArrayList<Comment>()
        for (comment in comments) {
            linearComments.add(comment)
            linearComments.addAll(getLinearCommentsList(comment.replies))
        }
        return linearComments
    }
}
