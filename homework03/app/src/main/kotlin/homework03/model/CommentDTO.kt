package homework03.model

data class CommentDTO(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val body: String?,
    val author: String,
    val children: List<CommentDTO>,
    val id: String,
    val topicId: String
)