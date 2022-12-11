package homework03

import java.util.*

data class TopicSnapshot(
    val topicSnapshotDate: Date, val creationDate: Date,
    val subscribersOnline: Int, val description: String, val discussions: List<Discussion>
)

data class Discussion(
    val id: String, val author: String, val publicationDate: Date,
    val votesUp: Int, val votesDown: Int, val title: String, val text: String, val htmlText: String
)

data class CommentsSnapshot(val commentsSnapshotData: Date, val comments: List<Comment>)

data class Comment(
    val id: String, val author: String, val replyTo: String, val discussionId: String, val depth: Int,
    val publicationDate: Date, val votesUp: Int, val votesDown: Int, val text: String
)