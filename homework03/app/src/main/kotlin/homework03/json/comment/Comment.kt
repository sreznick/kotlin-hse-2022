package homework03.json.comment

data class Comment(
    val created: Double,
    val ups: Long,
    val downs: Long,
    val body: String,
    val author: String,
    val replyTo: Long?,
    val replies: List<Comment>,
    val depth: Int,
    val id: Long,
    val snapshotTime: Long = System.currentTimeMillis()
)
