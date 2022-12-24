package homework03.json.topic

import com.fasterxml.jackson.annotation.JsonProperty

data class TopicInfoData(@JsonProperty("data") val data: TopicDetails) {
    data class TopicDetails(
        @JsonProperty("created") val created: Double,
        @JsonProperty("active_user_count") val activeUserCount: Long,
        @JsonProperty("ranking_size") val rankingSize: String?,
        @JsonProperty("public_description") val publicDescription: String,
    )
}