package homework03.networkingDTO.comments

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubCommentDTO(
        @JsonProperty("created") val created: Long,
        @JsonProperty("ups") val ups: Long,
        @JsonProperty("downs") val downs: Long,
        @JsonProperty("body") val body: String,
        @JsonProperty("author") val author: String,
        @JsonProperty("id") val id: String,
        @JsonProperty("depth") val depth: Int,
)
