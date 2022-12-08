package homework03.model

data class CommentDTO(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val selftext: String,
    val author: String,
    val children: List<CommentDTO>
)