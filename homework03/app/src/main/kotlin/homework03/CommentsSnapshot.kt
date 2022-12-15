package homework03

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


data class CommentsSnapshot(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val body: String,
    val author: String,
    var replyTo: Int,
    val replies: List<CommentsSnapshot>,
    val depth: Int,
    val id: Int
) {
    fun linearize(): List<CommentsSnapshot> {
        return listOf(this).plus(
            replies.map {
                it.linearize()
            }.flatten()
        )
    }
}


data class CommentInfo(val data: CommentInfoData)

data class CommentInfoData(val children: List<CommentInfoChildKind>)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes(
    JsonSubTypes.Type(CommentInfoChildKind1::class, name = "t1"),
    JsonSubTypes.Type(CommentInfoChildKind3::class, name = "t3")
)
open class CommentInfoChildKind(val kind: String)
data class CommentInfoChildKind1(val data: CommentInfoChild1) : CommentInfoChildKind("t1")
data class CommentInfoChildKind3(val data: CommentInfoChild3) : CommentInfoChildKind("t3")
data class CommentInfoChild3(
    val id: String,
    val selftext: String,
    val created: Long,
    val ups: Int,
    val author: String,
    val downs: Int
)

data class CommentInfoChild1(
    val id: String,
    val author: String,
    val created_utc: Long,
    val ups: Int,
    val downs: Int,
    val body: String,
    val replies: CommentInfo?
)

