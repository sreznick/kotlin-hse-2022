package homework03.networkingDTO.topic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicDetails(@JsonProperty("data") val data: TopicDetailsDTO) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TopicDetailsDTO(
            @JsonProperty("created") val created: Double,
            @JsonProperty("active_user_count") val activeUserCount: Long,
            @JsonProperty("ranking_size") val rankingSize: String?,
            @JsonProperty("public_description") val publicDescription: String,
    )
}
