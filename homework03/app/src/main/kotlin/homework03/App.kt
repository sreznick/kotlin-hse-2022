package homework03

import homework03.dto.comment.LinearComment
import homework03.dto.topic.TopicSnapshot
import homework03.lib.Utils
import homework03.lib.Utils.Companion.getLinear
import homework03.lib.csvSerialize
import homework03.lib.writeCsv
import kotlinx.coroutines.*
import java.io.FileOutputStream

class App {
    private val httpClient = RedditClient()
    val greeting: String
        get() {
            return "Hello World!"
        }

    private suspend fun parseComments(link: String, discussionId: String?): List<LinearComment> {
        val commentsRaw = httpClient.getComments(link)
        if (commentsRaw?.comments == null) {
            throw RuntimeException("Error while parsing comments")
        }
        return getLinear(commentsRaw.comments, discussionId)
    }


    private suspend fun parseTopicAndComments(name: String): Pair<List<TopicSnapshot.TopicDiscussion>, List<LinearComment>> =
        coroutineScope {
            val topic = httpClient.getTopic(name)
            if (topic.discussions == null) {
                throw RuntimeException("Topic $name parsing error - discussion field")
            }

            //Гарантируется за счет строки выше
            val linksPairs = Utils.getDiscussionLinks(topic)!!
            val linearComments = linksPairs.map { async { parseComments(it.second, it.first) } }.map { it.await() }.flatten()

            Pair(topic.discussions, linearComments)
        }

    suspend fun parseAndWrite(topics: List<String>, topicsFileName: String, commentsFileName: String) = coroutineScope {
        val asyncTasksResult = topics.map { async { parseTopicAndComments(it) } }.map { it.await() }
        val topicDiscussions = asyncTasksResult.map { it.first }.flatten()
        val linearComments = asyncTasksResult.map { it.second }.flatten()
        val serializedComments = csvSerialize(linearComments, LinearComment::class)
        val serializedTopics = csvSerialize(topicDiscussions, TopicSnapshot.TopicDiscussion::class)
        withContext(Dispatchers.IO) {
            FileOutputStream(commentsFileName).apply { writeCsv(serializedComments) }
        }
        withContext(Dispatchers.IO) {
            FileOutputStream(topicsFileName).apply { writeCsv(serializedTopics) }
        }
    }
}

fun main(args: Array<String>): Unit = runBlocking {
    if (args.isEmpty()) {
        println("Launch program: script [topics]")
    } else {
        try {
            App().parseAndWrite(args.toList(), "--subjects.csv", "--comments.csv")
            println("Success parsing")
        } catch (exception: RuntimeException) {
            println("Error while parsing: ${exception.message}")
        }
    }
}
