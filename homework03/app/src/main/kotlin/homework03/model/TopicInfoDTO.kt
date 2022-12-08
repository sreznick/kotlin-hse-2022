package homework03.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TopicInfoDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("author_fullname")
    val author: String,
    @JsonProperty("created")
    val created: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("selftext")
    val text: String,
    @JsonProperty("selftext_html")
    val textHtml: String?,
    @JsonProperty("ups")
    val ups: Int,
    @JsonProperty("downs")
    val downs: Int
)