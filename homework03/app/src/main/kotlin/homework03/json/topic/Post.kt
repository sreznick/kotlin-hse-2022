package homework03.json.topic

import com.fasterxml.jackson.annotation.JsonProperty

data class Post(
    @JsonProperty("author_fullname") val authorFullName: String,
    @JsonProperty("created") val created: Double,
    @JsonProperty("ups") val ups: Long,
    @JsonProperty("downs") val downs: Long,
    @JsonProperty("title") val title: String,
    @JsonProperty("selftext") val selfText: String?,
    @JsonProperty("selftext_html") val selfTextHtml: String?
)