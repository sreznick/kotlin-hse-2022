package homework03.models

data class Comment(
        val created: Long,
        val ups: Long,
        val downs: Long,
        val body: String,
        val author: String,
        val replyTo: String,
        val replies: List<Comment>,
        val depth: Int,
        val id: String,
        val snapshotTime: Long = System.currentTimeMillis()
)

data class CommentsSnapshot(val comments: List<Comment>)
