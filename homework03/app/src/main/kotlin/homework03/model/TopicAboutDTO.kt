package homework03.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TopicAboutDTO(

    @JsonProperty("created_utc")
    val created: Long,
    @JsonProperty("subscribers")
    val subscribers: Long,
    @JsonProperty("public_description")
    val description: String
)