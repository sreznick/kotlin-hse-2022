package homework03.json.topic

import com.fasterxml.jackson.annotation.JsonProperty

data class PostChildData(@JsonProperty("data") val data: PostChild) {
    data class PostChild(@JsonProperty("children") val children: List<PostData>) {
        data class PostData(@JsonProperty("data") val data: Post)
    }
}