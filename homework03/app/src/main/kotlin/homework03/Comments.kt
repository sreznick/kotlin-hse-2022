package homework03

import java.util.Date

data class Comment(
    val publicDate: Date,
    val upVotes: Int,
    val downVotes: Int,
    val selfText: String,
    val author: String,
    val depth: Int,
    val id: String,
    val replyTo: String,
    val title: String
)

data class CommentsSnapshot(
    val receivingDate: Date,
    val comments: List<Comment>
)
