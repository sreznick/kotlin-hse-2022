data class CommentsSnapshot(
    val receivingTime: Long,
    val comments: List<Comment>,
    val linearComments: List<Comment>
)