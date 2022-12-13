package homework03

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlin.collections.ArrayList

object RedditClient {
    private val httpClient = HttpClient(CIO)
    private val jacksonObjectMapper = ObjectMapper()

    suspend fun getTopic(name: String): TopicSnapshot {
        val json: String = httpClient.get("https://www.reddit.com/r/$name/.json").body()
        val jsonAbout: String = httpClient.get("https://www.reddit.com/r/$name/about.json").body()
        val jsonPath = "$.data.children[*].data"
        val jsonAboutPath = "$.data"
        val nodes: List<Map<String, Any?>> = JsonPath.read(json, jsonPath)
        val nodesAbout: Map<String, Any?> = JsonPath.read(jsonAbout, jsonAboutPath)
        val arr = nodes.map { jacksonObjectMapper.convertValue(it, TopicSnapshot.Post::class.java) }
        return jacksonObjectMapper.convertValue(nodesAbout + mapOf("posts" to arr), TopicSnapshot::class.java)
    }

    suspend fun getComments(topic: String, title: String): CommentsSnapshot {
        val json: String = httpClient.get("https://www.reddit.com/r/$topic/comments/$title/.json").body()
        val commentClass = CommentsSnapshot.Comment::class.java
        fun MutableMap<String, Any?>.fixParentId() {
            put("parent_id", (get("parent_id") as String).substring(3))
        }
        fun getAsTree(): List<CommentsSnapshot.Comment> {
            val jsonTree = jacksonObjectMapper.readTree(json).get(1).get("data").get("children")

            fun getSubtree(tree: JsonNode): CommentsSnapshot.Comment {
                val repliesJson = tree.get("replies")
                val replies: MutableMap<String, Any?> =
                    jacksonObjectMapper.convertValue(tree, object : TypeReference<MutableMap<String, Any?>>() {})
                replies.fixParentId()
                if (repliesJson?.isEmpty != false) {
                    return try {
                        jacksonObjectMapper.convertValue(replies, commentClass)
                    } catch (e: IllegalArgumentException) {
                        jacksonObjectMapper.convertValue(replies + mapOf("replies" to null), commentClass)
                    }
                }
                val result = ArrayList<CommentsSnapshot.Comment>()
                repliesJson.get("data").get("children").forEach { result.add(getSubtree(it.get("data"))) }
                return jacksonObjectMapper.convertValue(replies + mapOf("replies" to result), commentClass)
            }

            val result: ArrayList<CommentsSnapshot.Comment> = arrayListOf()
            jsonTree.forEach { result.add(getSubtree(it.get("data"))) }
            return result
        }

        fun getAsList(): List<CommentsSnapshot.Comment> {
            val jsonPath = "$.[1].data.children[*].data"
            val nextLevelPath = ".replies.data.children[*].data"
            val jsonReplies = StringBuilder(jsonPath)
            val nodes: ArrayList<MutableMap<String, Any?>> = JsonPath.read(json, jsonPath)

            while (true) {
                jsonReplies.append(nextLevelPath)
                val replies: List<MutableMap<String, Any?>> = JsonPath.read(json, jsonReplies.toString())
                if (replies.isEmpty()) {
                    break
                }
                nodes.addAll(replies)
            }
            nodes.forEach {
                it["replies"] = null
                it.fixParentId()
            }
            return nodes.map { jacksonObjectMapper.convertValue(it, commentClass) }
        }

        return CommentsSnapshot(getAsTree(), getAsList())
    }
}