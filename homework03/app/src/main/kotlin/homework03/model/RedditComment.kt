package homework03.model

import homework03.model.json.RedditCommentJson

data class RedditComment(
    val created: Long,
    val score: Int,
    val author: String,
    val id: String,
    val topicId: String,
    val replyTo: String?,
    val depth: Int
) {
    companion object {
        fun fromJson(json: RedditCommentJson, topicId: String, replyTo: String?) =
            RedditComment(json.created, json.score, json.author, json.id, topicId, replyTo, json.depth)
    }
}
