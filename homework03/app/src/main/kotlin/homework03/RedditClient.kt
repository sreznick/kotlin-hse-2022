package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import kotlinx.coroutines.*
import java.util.*

class RedditClient {
    private val client = createHttpClient()
    private val supervisor = CoroutineScope(SupervisorJob())
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    suspend fun getTopic(topicName: String): TopicSnapshot = coroutineScope {
        val urls = listOf(
            "https://www.reddit.com/r/${topicName}/about.json",
            "https://www.reddit.com/r/${topicName}.json"
        )
        val requests = urls.map { requestAsync(it).cover() }
        val (topicInfoResponse, topicDiscussionsResponse) = awaitAll(*requests.toTypedArray()).map {
            it ?: throw RedditParsingException()
        }
        val topicInfoTree = mapper.readTree(topicInfoResponse.readAllBytes()).path("data")
        val topicDiscussionsTree = mapper.readTree(topicDiscussionsResponse.readAllBytes())
            .path("data").path("children")

        val topicDiscussions = arrayListOf<Discussion>()
        for (node in topicDiscussionsTree) {
            val discussionTree = node.path("data")
            val id = discussionTree.path("id").asText()
            val author = discussionTree.path("author").asText()
            val publicationDate = Date(discussionTree.path("created").asLong() * 1000)
            val votesUp = discussionTree.path("ups").asInt()
            val votesDown = discussionTree.path("downs").asInt()
            val title = discussionTree.path("title").asText()
            val text = discussionTree.path("selftext").asText()
            val htmlText = discussionTree.path("selftext_html").asText()
            topicDiscussions.add(Discussion(id, author, publicationDate, votesUp, votesDown, title, text, htmlText))
        }

        val creationDate = Date(topicInfoTree.path("created").asLong() * 1000)
        val subscribersOnline = topicInfoTree.path("active_user_count").asInt()
        val description = topicInfoTree.path("public_description").asText()

        TopicSnapshot(Date(), creationDate, subscribersOnline, description, topicDiscussions)
    }

    suspend fun getComments(discussionId: String): CommentsSnapshot = coroutineScope {
        val url = "https://www.reddit.com/comments/${discussionId}.json"
        val request = requestAsync(url).cover()
        val commentsResponse = request.await() ?: throw RedditParsingException()
        val commentsTree = (mapper.readTree(commentsResponse.readAllBytes()) as ArrayNode).get(1)
            .path("data").path("children")

        val queue = ArrayDeque<JsonNode>()
        for (comment in commentsTree) {
            queue.add(comment.path("data"))
        }
        val comments = arrayListOf<Comment>()
        while (!queue.isEmpty()) {
            val comment = queue.removeFirst()
            val id = comment.path("id").asText()
            val author = comment.path("author").asText()
            val parentId = comment.path("parent_id").asText()
            val replyTo = if (parentId.startsWith("t3_")) "" else parentId.substring(3)
            val depth = comment.path("depth").asInt()
            val publicationDate = Date(comment.path("created").asLong() * 1000)
            val votesUp = comment.path("ups").asInt()
            val votesDown = comment.path("downs").asInt()
            val text = comment.path("body").asText()
            comments.add(Comment(id, author, replyTo, discussionId, depth, publicationDate, votesUp, votesDown, text))
            val replies = comment.path("replies")
            if (replies.isObject) {
                for (reply in replies.path("data").path("children")) {
                    queue.add(reply.path("data"))
                }
            }
        }

        CommentsSnapshot(Date(), comments)
    }

    suspend fun saveTopicInfo(topicName: String, path: String) = coroutineScope {
        val topicSnapshot = getTopic(topicName)
        val commentsRequests =
            topicSnapshot.discussions.map { supervisor.async { getComments(it.id).comments }.cover() }
        val comments = awaitAll(*commentsRequests.toTypedArray()).flatMap { it ?: throw RedditParsingException() }
        launch(Dispatchers.IO) {
            writeCsvToFile(
                path,
                "${topicName}-subjects.csv",
                csvSerialize(topicSnapshot.discussions, Discussion::class)
            )
            println("Wrote file ${topicName}-subjects.csv")
        }
        launch(Dispatchers.IO) {
            writeCsvToFile(
                path,
                "${topicName}-comments.csv",
                csvSerialize(comments, Comment::class)
            )
            println("Wrote file ${topicName}-comments.csv")
        }
    }

    private fun requestAsync(url: String) = supervisor.async {
        client.request(Http.Method.GET, url)
    }

    private fun <T> Deferred<T>.cover(): Deferred<T?> = supervisor.async {
        try {
            await()
        } catch (e: Exception) {
            null
        }
    }
}

class RedditParsingException(reason: String = "problems with parsing Reddit") : Exception(reason)