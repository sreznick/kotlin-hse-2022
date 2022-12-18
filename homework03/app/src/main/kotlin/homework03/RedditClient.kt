package homework03

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.HttpClient
import com.soywiz.korio.net.http.createHttpClient
import java.util.*


data class TopicSnapshot(
    val id: Int,
    val dateTaken: Date,
    val about: AboutSnapshot,
    val topics: List<HotSnapshot>
)

data class CommentSnapshot(
    val dateTaken: Date,
    val postId: String,
    val comments: List<Comment>
)

class RedditClient {
    private var topicId = 1

    private val mapper = jacksonObjectMapper()

    companion object {
        @JvmStatic
        private val postIdIndexInSplit = 6
    }


    private suspend fun HttpClient.getJsonString(name: String) =
        request(
            Http.Method.GET,
            name, Http.Headers.build {
                put("Accept-Encoding", "application/json")
            }).readAllString()

    private fun ObjectMapper.readAboutSection(section: String) =
        readValue<AboutSnapshot>(readValue<JsonNode>(section).get("data"))

    private inline fun <reified T> ObjectMapper.readValue(node: JsonNode) = readValue<T>(node.toString())

    suspend fun getAndSaveTopic(name: String) {
        val topicHotPosts: List<HotSnapshot> = getTopic(name).topics
        writeCsv("$name--subjects.csv", csvSerialize(topicHotPosts, HotSnapshot::class))
        val comments: List<CommentSnapshot> = topicHotPosts.map { getComments(it.permalink) }
        writeCsv("$name--comments.csv", csvSerialize(comments, CommentSnapshot::class))
    }


    suspend fun getTopic(name: String): TopicSnapshot {
        val client = createHttpClient()
        val aboutTopicJSON = client.getJsonString("https://www.reddit.com/r/$name/about.json")

        val topicJson = client.getJsonString("https://www.reddit.com/r/$name/.json")


        val about: AboutSnapshot = mapper.readAboutSection(aboutTopicJSON)
        val hot: JsonNode = mapper.readValue(topicJson)

        val nodeList: List<JsonNode> = mapper.readValue(hot["data"]["children"])
        val snapList: List<HotSnapshot> = nodeList.map { mapper.readValue(it["data"]) }

        return TopicSnapshot(topicId++, Date(), about, snapList)
    }

    suspend fun getComments(title: String): CommentSnapshot {
        val client = createHttpClient()
        val jsonString = client.request(
            Http.Method.GET,
            "$title.json", Http.Headers.build {
                put("Accept-Encoding", "application/json")
            }).readAllString()
        val baseComments: JsonNode = mapper.readValue(jsonString)
        val list: List<Comment> = mapper.readValue(baseComments[1]["data"]["children"])
        return CommentSnapshot(Date(), title.split("/")[postIdIndexInSplit], list)
    }

}