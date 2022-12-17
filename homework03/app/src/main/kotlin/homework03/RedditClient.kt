package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.http.createHttpClient
import kotlinx.coroutines.*
import java.net.CacheResponse
import java.net.URL
import java.util.*
import kotlin.collections.ArrayDeque

class RedditClient{
    private val client = createHttpClient()
    private val jsonMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val mainCoroutine = CoroutineScope(SupervisorJob());

    suspend fun getTopic(title: String) : TopicSnapShot = coroutineScope {
        val content = listOf(
            makeRequest(getTopicUrl(title,"/about.json")).getContent(),
            makeRequest(getTopicUrl(title, ".json")).getContent()
        )
        val (topic, discussion) = awaitAll(*content.toTypedArray()).map {it ?: throw Exception("Smth went wrong")}
        val topicInfo = getJsonTree(topic).path("data")
        val discussionInfo = getJsonTree(discussion).path("data").path("children")

        wrapTopicSnapShot(topicInfo, discussionInfo);
    }

    suspend fun getComments(title: String) : CommentsSnapshot = coroutineScope {
        val content = makeRequest(getCommentUrl(title)).getContent()
        val comment = content.await() ?: throw Exception("Smth went wrong")
        val commentInfo = getJsonTreeWithoutPrefix(comment, 1).path("data").path("children")
        wrapCommentsSnapshot(commentInfo, title)
    }

    suspend fun parse(title: String, path: String) = coroutineScope {
        val topic = getTopic(title);
        val parsedTopic = csvSerialize(topic.discussion, Discussion::class)
        val parsedComments = csvSerialize(awaitAll(*topic.discussion.map {
            mainCoroutine.async {
                getComments(it.id).comments
            }.getContent()
        }.toTypedArray()).flatMap { it ?: throw Exception("Smth went wrong")}, Comment::class)
        launch {
            CsvWriter.write(parsedTopic, path, "${title}-subjects.csv")
            println("Topics parsed to ${title}-subjects.csv")
        }
        launch {
            CsvWriter.write(parsedComments, path, "${title}-comments.csv")
            println("Comments parsed to ${title}-comments.csv")
        }
    }

    private fun getTopicUrl(topic: String, path: String) : String = "https://www.reddit.com/r/${topic}${path}"
    private fun getCommentUrl(comment: String) : String = "https://www.reddit.com/comments/${comment}.json"
    private fun convertLongToDate(time: Long) : Date = Date(time * 1000);
    private fun makeRequest(url: String) = mainCoroutine.async { client.request(Http.Method.GET, url) }
    private fun <T> Deferred<T>.getContent(): Deferred<T?> = mainCoroutine.async {
        try {
            await()
        }
        catch (error: Exception) {
            null
        }
    }

    private suspend fun getJsonTree(response: HttpClient.Response) = jsonMapper.readTree(response.readAllBytes())
    private suspend fun getJsonTreeWithoutPrefix(response: HttpClient.Response, prefix: Int) : JsonNode {
        return (jsonMapper.readTree(response.readAllBytes()) as ArrayNode).get(prefix)
    }
    private suspend fun wrapDiscussion(content: JsonNode): Discussion {
        val selfText = content.path("data").path("selftext").asText()
        val selfHtmlText = content.path("data").path("selftext_html").asText()
        val upVotes = content.path("data").path("ups").asInt()
        val downVotes = content.path("data").path("downs").asInt()
        val author = content.path("data").path("author").asText()
        val title = content.path("data").path("title").asText()
        val id = content.path("data").path("id").asText()
        val publicDate = convertLongToDate(content.path("data").path("created").asLong())
        return Discussion(author, publicDate, upVotes, downVotes, title, selfText, selfHtmlText, id)
    }

    private suspend fun wrapTopicSnapShot(contentTopic: JsonNode, contentDiscussion: JsonNode): TopicSnapShot {
        val description = contentTopic.path("public_description").asText()
        val publicDate = convertLongToDate(contentTopic.path("created").asLong())
        val onlineSubs = contentTopic.path("active_user_count").asInt()
        val discussion = arrayListOf<Discussion>();
        for (element in contentDiscussion) {
            discussion.add(wrapDiscussion(element));
        }
        return TopicSnapShot(publicDate, onlineSubs, description, discussion, Date())
    }
    private suspend fun wrapComment(content: JsonNode, discussionTitle: String) : Comment {
        val publicDate = convertLongToDate(content.path("created").asLong())
        val upVotes = content.path("ups").asInt()
        val downVotes = content.path("downs").asInt()
        val selfText = content.path("body").asText()
        val depth = content.path("depth").asInt();
        val id = content.path("id").asText()
        val author = content.path("author").asText()
        val replyTo = content.path("parent_id").asText()
        return Comment(publicDate, upVotes, downVotes, selfText, author, depth, id, replyTo, discussionTitle)
    }

    private suspend fun wrapCommentsSnapshot(content: JsonNode, discussionTitle: String) : CommentsSnapshot {
        val q = ArrayDeque<JsonNode>()
        content.map { q.add(it.path("data")) }
        val processed = arrayListOf<Comment>()
        while (!q.isEmpty()) {
            val children = q.first().path("replies")
            processed.add(wrapComment(q.removeFirst(), discussionTitle));
            if (!children.isObject) continue
            children.path("data").path("children").map { q.add(it.path("data")) }
        }
        return CommentsSnapshot(Date(), processed)
    }
}
