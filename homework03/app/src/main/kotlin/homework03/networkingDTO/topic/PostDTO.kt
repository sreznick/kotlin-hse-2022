package homework03.networkingDTO.topic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PostDTO(
        @JsonProperty("author_fullname") val author: String,
        @JsonProperty("created") val created: Double,
        @JsonProperty("ups") val ups: Long,
        @JsonProperty("downs") val downs: Long,
        @JsonProperty("title") val title: String,
        @JsonProperty("selftext") val text: String?,
        @JsonProperty("selftext_html") val htmlText: String?
)
