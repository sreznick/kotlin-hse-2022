package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicSnapshot(
    @JsonProperty("created")
    val creationTime : BigDecimal,
    @JsonProperty("active_user_count")
    val onlineSubs : Int,
    @JsonProperty("ranking_size")
    val size : String?,
    @JsonProperty("public_description")
    val description : String,
    @JsonProperty("posts")
    val posts : List<Post>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Post(
        @JsonProperty("selftext")
        val selftext: String?,
        @JsonProperty("author")
        val author: String,
        @JsonProperty("created_utc")
        val created_utc: BigDecimal,
        @JsonProperty("ups")
        val ups: Int,
        @JsonProperty("downs")
        val downs: Int,
        @JsonProperty("title")
        val title: String,
        @JsonProperty("selftext_html")
        val selftext_html: String?,
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentsSnapshot(val commentsAsTree : List<Comment>, val commentsAsList: List<Comment>) {
    private val getTime = System.currentTimeMillis()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class  Comment(
        @JsonProperty("created_utc")
        val created_utc: BigDecimal,
        @JsonProperty("ups")
        val ups: Int,
        @JsonProperty("downs")
        val downs : Int,
        @JsonProperty("body")
        val text : String?,
        @JsonProperty("author")
        val author: String,
        @JsonProperty("id")
        val id : String,
        @JsonProperty("parent_id")
        val replyTo : String?,
        @JsonProperty("depth")
        val depth : Int,
        @JsonProperty("replies")
        val replies : List<Comment>?
    )
}