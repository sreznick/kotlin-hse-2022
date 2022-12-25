package homework03.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonSubredditInfoRepresentation (
    val data: SubredditInfo
        ) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SubredditInfo(
        @JsonProperty("created")
        val creationTime: Long,
        @JsonProperty("subscribers")
        val subscribersOnline: Int,
        val public_description: String,
        val id: String
    )
}