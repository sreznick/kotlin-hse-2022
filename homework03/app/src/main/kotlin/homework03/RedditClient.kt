package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import kotlinx.coroutines.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class RedditClient {

    private val client = createHttpClient()
    private val supervisor = CoroutineScope(SupervisorJob())
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private fun requestAsync(url: String) = supervisor.async {
        client.request(Http.Method.GET, url)
    }

    private fun requestTopicAboutAsync(name: String) = requestAsync("https://www.reddit.com/r/${name}/about.json")

    private fun requestTopicDiscussionsAsync(name: String) = requestAsync("https://www.reddit.com/r/${name}.json")

    private fun requestDiscussionCommentsAsync(id: String) = requestAsync("https://www.reddit.com/comments/${id}.json")

    private fun <T> Deferred<T>.cover(): Deferred<T> = supervisor.async {
        try {
            await()
        } catch (e: Exception) {
            println("Something went wrong: ${e.message}")
            exitProcess(1)
        }
    }

    private suspend fun getTopic(name: String): TopicSnapshot = coroutineScope {
        val requestCreators = listOf(::requestTopicAboutAsync, ::requestTopicDiscussionsAsync)
        val requests = requestCreators.map { it(name).cover() }
        val (topicAboutResponse, topicDiscussionsResponse) = awaitAll(*requests.toTypedArray())

        val topicAbout = mapper.readValue(topicAboutResponse.readAllBytes(), TopicWrapper::class.java).data
        val topicDiscussions = mapper.readValue(
            topicDiscussionsResponse.readAllBytes(), TopicDiscussionsWrapper::class.java
        ).data.discussions.map { it.data }

        TopicSnapshot(topicAbout, topicDiscussions)
    }

    private suspend fun getComments(id: String): CommentsSnapshot = coroutineScope {
        val request = requestDiscussionCommentsAsync(id).cover()
        val discussionCommentsResponse = request.await()

        val discussionData = mapper.readTree(discussionCommentsResponse.readAllBytes())
        (discussionData as ArrayNode).remove(0)
        val discussionComments = discussionData.get(0).path("data").path("children")

        val comments = ArrayList<Comment>()

        val queue = ArrayDeque<JsonNode>()
        for (commentData in discussionComments) {
            queue.add(commentData)
        }
        while (queue.isNotEmpty()) {
            val comment = queue.removeFirst().path("data")
            val commentId = comment.path("id").textValue()
            val parentIdRow = comment.path("parent_id").textValue()
            val parentId = if (parentIdRow.substring(0, 2) == "t3") "" else parentIdRow.substring(3)
            val depth = comment.path("depth").intValue()
            val author = comment.path("author").textValue()
            val publicationDate = Date(comment.path("created").longValue() * 1000)
            val votesUp = comment.path("ups").intValue()
            val votesDown = comment.path("downs").intValue()
            val text = comment.path("body").textValue() ?: ""
            comments.add(Comment(commentId, parentId, id, depth, author, publicationDate, votesUp, votesDown, text))

            if (comment.path("replies").isObject) {
                for (reply in comment.path("replies").path("data").path("children")) {
                    queue.add(reply)
                }
            }
        }

        CommentsSnapshot(Date(), comments)
    }

    suspend fun getAndSaveTopicInfo(name: String, path: String) = coroutineScope {
        val topicDiscussions = getTopic(name).discussions
        val comments = awaitAll(*topicDiscussions.map { supervisor.async { getComments(it.id) }.cover() }
            .toTypedArray()).flatMap { it.comments }

        fun <T : Any> writeCsv(iterable: Iterable<T>, iterableClass: KClass<T>, filename: String) =
            launch(Dispatchers.IO) {
                CsvWriter.writeCsv(
                    path, "${name}--${filename}.csv", CsvSerializer.csvSerialize(iterable, iterableClass)
                )
                println("Created file ${name}--${filename}.csv")
            }
        writeCsv(topicDiscussions, Discussion::class, "subjects")
        writeCsv(comments, Comment::class, "comments")
    }
}
