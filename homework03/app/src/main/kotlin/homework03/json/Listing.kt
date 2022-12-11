package homework03.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Listing(
    val data: ListingData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ListingData(
        val children: List<JsonDiscussionRepresentation>
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class JsonDiscussionRepresentation(
            val data: DiscussionInformation
        )
    }
}