package homework03

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubComment(
    @JsonProperty("created") val created: Long,
    @JsonProperty("ups") val ups: Long,
    @JsonProperty("downs") val downs: Long,
    @JsonProperty("body") val body: String,
    @JsonProperty("author") val author: String,
    @JsonProperty("id") val id: String,
    @JsonProperty("depth") val depth: Int,
)

data class Comment(
    val created: Long,
    val ups: Long,
    val downs: Long,
    val body: String,
    val author: String,
    val replyTo: String,
    val replies: List<Comment>,
    val depth: Int,
    val id: String,
    val snapshotTime: Long = System.currentTimeMillis()
)

data class CommentsSnapshot(
    val comments: List<Comment>
)