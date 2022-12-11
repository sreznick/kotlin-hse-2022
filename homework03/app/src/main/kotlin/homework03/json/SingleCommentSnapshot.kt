package homework03.json

internal data class SingleCommentSnapshot(
    val publicationDate: Long,
    val ups: Int,
    val downs: Int,
    val text: String?,
    val author: String?,
    val id: String,
    val replyTo: String,
    val depth: Int

)
