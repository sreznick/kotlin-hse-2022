package homework03.dto.comment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import homework03.dto.Created

@JsonIgnoreProperties(ignoreUnknown = true)
class CommentsJson {
    val data: CommentJsonData? = null
    @JsonIgnoreProperties(ignoreUnknown = true)
    class CommentJsonData{
        val children: List<CommentsData>? = null
        @JsonIgnoreProperties(ignoreUnknown = true)
        class CommentsData{
            val data: CommentData? = null;
            @JsonIgnoreProperties(ignoreUnknown = true)
            class CommentData{
                val created: Created? = null
                val ups: Int? = null
                val downs: Int? = null
                val body: String? = null
                val body_html: String? = null
                val depth: Int? = null
                val replies: CommentsJson? = null
                val author_fullname: String? = null
            }
        }

    }
}