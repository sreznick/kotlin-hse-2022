package homework03.model

import java.util.Date

data class CommentsSnapshot(
    val comments: List<CommentDTO>,
    val date: Date
)