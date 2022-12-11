package homework03

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class TopicSnapshot(
    val snapshotDate: Date,
    val creationDate: Date,
    val numberOfSubscribersOnline: Int,
    val description: String,
    val discussions: List<Discussion>
) {
    constructor(topic: Topic, discussions: List<Discussion>) : this(
        Date(), Date(topic.created * 1000), topic.active_user_count, topic.public_description, discussions
    )
}

data class Discussion(
    val id: String,
    val author: String,
    val publicationDate: Date,
    val numberOfVotesUp: Int,
    val numberOfVotesDown: Int,
    val title: String,
    val text: String,
    val htmlText: String
) {
    @JsonCreator
    constructor(
        id: String,
        title: String,
        author: String,
        created: Long,
        ups: Int,
        downs: Int,
        selftext: String?,
        selftext_html: String?
    ) : this(id, author, Date(created * 1000), ups, downs, title, selftext ?: "", selftext_html ?: "")

}

data class TopicWrapper(val data: Topic)

data class Topic(
    val created: Long,
    val active_user_count: Int,
    val public_description: String,
)

data class TopicDiscussionsWrapper(val data: TopicDiscussions)

data class TopicDiscussions(@JsonProperty("children") val discussions: List<TopicDiscussionWrapper>)

data class TopicDiscussionWrapper(val data: Discussion)

data class CommentsSnapshot(val snapshotDate: Date, val comments: List<Comment>)

data class Comment(
    val id: String,
    val replyTo: String,
    val discussionId: String,
    val depth: Int,
    val author: String,
    val publicationDate: Date,
    val numberOfVotesUp: Int,
    val numberOfVotesDown: Int,
    val text: String
)
