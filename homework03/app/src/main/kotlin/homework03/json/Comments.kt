package homework03.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

var commentsCount = 0L

data class Comment(
    val creationTime: Double,
    val likes: Long,
    val dislikes: Long,
    val text: String,
    val author: String,
    val replyTo: Long?,
    val replies: List<Comment>,
    val depth: Int,
    val id: Long
) {
    val snapshotTime = System.currentTimeMillis()
}

data class CommentsSnapshot(
    val comments: List<Comment>
) {
    fun linearize(): List<Comment> {
        val result: MutableList<Comment> = arrayListOf()

        fun recursive(comm: Comment) {
            result.add(comm)
            for (child in comm.replies) {
                recursive(child)
            }
        }

        comments.forEach(::recursive)
        return result
    }

    companion object {
        fun deserialize(objectMapper: ObjectMapper, json: String): CommentsSnapshot {

            fun deserializeComment(comm: JsonNode, parentId: Long?, depth: Int): Comment = with(comm.get("data")) {
                val curId = commentsCount++
                val children: MutableList<Comment> = arrayListOf()
                try {
                    for (child in get("replies").get("data").get("children")) {
                        children.add(deserializeComment(child, curId, depth + 1))
                    }
                }
                catch (_: NullPointerException) {}

                return Comment(
                    creationTime = get("created").asDouble(),
                    likes = get("ups").asLong(),
                    dislikes = get("downs").asLong(),
                    text = get("body").toPrettyString(),
                    author = get("author_fullname").toPrettyString(),
                    replyTo = parentId,
                    replies = children,
                    depth = depth,
                    id = curId
                )
            }

            val result: MutableList<Comment> = arrayListOf()
            val topLevelComments = objectMapper.readTree(json).get(1).get("data").get("children")
            for (comm in topLevelComments) {
                result.add(deserializeComment(comm, null, 0))
            }
            return CommentsSnapshot(result)
        }
    }
}