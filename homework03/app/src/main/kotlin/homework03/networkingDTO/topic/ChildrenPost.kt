package homework03.networkingDTO.topic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChildrenPost(@JsonProperty("data") val data: ChildrenPostDTO) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ChildrenPostDTO(@JsonProperty("children") val children: List<PostData>) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class PostData(@JsonProperty("data") val data: PostDTO)
    }
}
