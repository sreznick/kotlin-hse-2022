package homework03.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Post(
    @JsonProperty("author_fullname") val author: String,
    @JsonProperty("created") val created: Double,
    @JsonProperty("ups") val ups: Long,
    @JsonProperty("downs") val downs: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("selftext") val text: String?,
    @JsonProperty("selftext_html") val htmlText: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChildrenPostData(@JsonProperty("data") val data: ChildrenPost) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChildrenPost(@JsonProperty("children") val children: List<PostData>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class PostData(@JsonProperty("data") val data: Post)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicDetailsData(@JsonProperty("data") val data: TopicDetails) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TopicDetails(
        @JsonProperty("created") val created: Double,
        @JsonProperty("active_user_count") val activeUserCount: Long,
        @JsonProperty("ranking_size") val rankingSize: String?,
        @JsonProperty("public_description") val publicDescription: String,
    )
}

data class TopicSnapshot(
    val creationTime: Double,
    val subscribersOnline: Long,
    val description: String,
    val posts: List<Post>
)
