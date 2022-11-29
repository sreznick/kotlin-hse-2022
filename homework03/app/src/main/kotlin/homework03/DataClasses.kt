package homework03

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

data class Post(
    @JsonProperty("author_fullname") val author: String,
    @JsonProperty("created") val creationTime: Double,
    @JsonProperty("ups") val upvotes: Long,
    @JsonProperty("downs") val downvotes: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("selftext") val text: String?,
    @JsonProperty("selftext_html") val htmlText: String?
)

data class JsonPostsWrapper(@JsonProperty("data") val data: JsonPosts) {
    data class JsonPosts(@JsonProperty("children") val jsonPostWrappers: List<JsonPostWrapper>) {
        data class JsonPostWrapper(@JsonProperty("data") val data: Post)
    }
}

data class JsonTopicInfoWrapper(@JsonProperty("data") val data: TopicInfo) {
    data class TopicInfo(
        @JsonProperty("created") val creationTime: Double,
        @JsonProperty("active_user_count") val subscribersOnline: Long,
        @JsonProperty("ranking_size") val sizeRanking: String?,
        @JsonProperty("public_description") val description: String,
    )
}

var topicsCount = 0L

data class TopicSnapshot(
    val creationTime: Double,
    val subscribersOnline: Long,
    val sizeRanking: String?,
    val description: String,
    val jsonPostWrappers: List<Post>
) {
    val snapshotTime = System.currentTimeMillis()
    val id = topicsCount++

    companion object {
        fun create(mainInfo: JsonTopicInfoWrapper, posts: JsonPostsWrapper) =
            TopicSnapshot(
                creationTime = mainInfo.data.creationTime,
                subscribersOnline = mainInfo.data.subscribersOnline,
                sizeRanking = mainInfo.data.sizeRanking,
                description = mainInfo.data.description,
                jsonPostWrappers = posts.data.jsonPostWrappers.map {it.data}
            )
    }
}

var commentsCount = 0L

data class Comment(
    val creationTime: Double,
    val likes: Long,
    val dislikes: Long,
    val text: String,
    val author: String,
    val replyTo: Long?,
    val replies: List<Comment>,
    val depth: Int,
    val id: Long
) {
    val snapshotTime = System.currentTimeMillis()
}

data class CommentsSnapshot(
    val comments: List<Comment>
) {
    fun linearize(): List<Comment> {
        val result: MutableList<Comment> = arrayListOf()

        fun recursive(comm: Comment) {
            result.add(comm)
            for (child in comm.replies) {
                recursive(child)
            }
        }

        comments.forEach(::recursive)
        return result
    }

    companion object {
        fun deserialize(objectMapper: ObjectMapper, json: String): CommentsSnapshot {

            fun deserializeComment(comm: JsonNode, parentId: Long?, depth: Int): Comment = with(comm.get("data")) {
                val curId = commentsCount++
                val children: MutableList<Comment> = arrayListOf()
                try {
                    for (child in get("replies").get("data").get("children")) {
                        children.add(deserializeComment(child, curId, depth + 1))
                    }
                }
                catch (_: NullPointerException) {}

                return Comment(
                    creationTime = get("created").asDouble(),
                    likes = get("ups").asLong(),
                    dislikes = get("downs").asLong(),
                    text = get("body").toPrettyString(),
                    author = get("author_fullname").toPrettyString(),
                    replyTo = parentId,
                    replies = children,
                    depth = depth,
                    id = curId
                )
            }

            val result: MutableList<Comment> = arrayListOf()
            val topLevelComments = objectMapper.readTree(json).get(1).get("data").get("children")
            for (comm in topLevelComments) {
                result.add(deserializeComment(comm, null, 0))
            }
            return CommentsSnapshot(result)
        }
    }
}
