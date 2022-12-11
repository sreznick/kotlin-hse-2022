package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.fasterxml.jackson.module.kotlin.readValue
import homework03.json.*
import homework03.json.Listing
import homework03.json.SingleCommentSnapshot
import homework03.json.TopicSnapshot
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*


internal class RedditClient {
    suspend fun getTopic(name: String): TopicSnapshot =

        coroutineScope {

            val mapper = jacksonObjectMapper()

            val client = HttpClient(CIO)
            val json = async {
                val request = client.request("https://www.reddit.com/r/$name/.json")
                if (request.status != HttpStatusCode(200, "OK")) throw RedditClientException.PageGettingException(name)
                request.bodyAsText()

            }
            val jsonInfo = async {
                val request = client.request("https://www.reddit.com/r/$name/about.json")
                if (request.status != HttpStatusCode(200, "OK")) throw RedditClientException.PageGettingException(name)
                request.bodyAsText()
            }

            val discussions = async {
                val discussionsInfo: Listing = mapper.readValue(json.await())
                discussionsInfo.data.children.map { it.data }
            }


            try {
                val info: JsonSubredditInfoRepresentation = mapper.readValue(jsonInfo.await())
                val subredditInfo = info.data
                return@coroutineScope TopicSnapshot(
                    subredditInfo.creationTime,
                    subredditInfo.subscribersOnline,
                    subredditInfo.public_description,
                    discussions.await(),
                    subredditInfo.id
                )
            } catch (e: MissingKotlinParameterException) {
                throw RedditClientException.JsonParsingException("Error in $name: Invalid Json file given")
            }

        }


    suspend fun getComments(name: String): CommentsSnapshot {

        val client = HttpClient(CIO)
        val request = client.request("https://www.reddit.com/$name/.json")
        if (request.status != HttpStatusCode(200, "OK")) throw RedditClientException.PageGettingException(name)
        val json = request.bodyAsText()
        val mapper = jacksonObjectMapper()
        val commentsTree: JsonNode = mapper.readTree(json)
        val commentsList: MutableList<SingleCommentSnapshot> = ArrayList()

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class CommentInfo(
            val body: String?,
            val ups: Int,
            val downs: Int,
            val created: Long,
            val id: String,
            val author: String?,
            val depth: Int
        )

        fun addComments(parent: JsonNode, replyTo: String) {
            val commentInfo: CommentInfo =
                mapper.treeToValue(parent) ?: throw RedditClientException.JsonParsingException()
            commentsList.add(
                SingleCommentSnapshot(
                    commentInfo.created,
                    commentInfo.ups,
                    commentInfo.downs,
                    commentInfo.body,
                    commentInfo.author,
                    commentInfo.id,
                    replyTo,
                    commentInfo.depth
                )
            )
            if (parent["replies"] != null && !parent["replies"].isEmpty) {
                for (reply in parent["replies"]["data"]["children"]) {
                    val data = reply["data"]
                    addComments(data, commentInfo.id)
                }
            }
        }

        for (comment in commentsTree[1]["data"]["children"]) {
            val data = comment["data"]
            addComments(data, name)
        }
        return CommentsSnapshot(commentsList)
    }

}

sealed class RedditClientException(reason: String) : RuntimeException(reason) {

    class PageGettingException(name: String) : RedditClientException("Can't get page with name \"$name\"")

    class JsonParsingException(name: String = "Something went wrong during parsing json data") : RedditClientException(name)
}