package homework03

import com.fasterxml.jackson.annotation.JsonProperty

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

data class CommentSnapshot(
    val creationTime: Double,
    val likes: Long,
    val dislikes: Long,
    val text: String,
    val author: String,
    val replyTo: Long?,
    val replies: List<CommentSnapshot>,
    val depth: Int
) {
    val snapshotTime = System.currentTimeMillis()
    val id = commentsCount + 1
}
