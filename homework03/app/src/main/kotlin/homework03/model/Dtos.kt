package homework03.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

open class CommentDTO(
    private val created: Long,
    private val ups: Int,
    private val downs: Int,
    val text: String,
    val author: String,
    val replies: List<CommentDTO>,
    val id: String,
    val topicId: String
) : Listable {
    override fun toListComment(): ListComment = ListComment(created, ups, downs, text, author, id, topicId)
}

data class RedditDataDTO(
    val data: CommentChildrenDTO
) : RawCommentsDTO {
    override fun toCommentsDTO(topicId: String): List<CommentDTO> = data.toCommentsDTO(topicId)
}

data class CommentDataDTO(
    val data: RawCommentT
) : RawCommentT {
    override fun toCommentDTO(topicId: String): CommentDTO = data.toCommentDTO(topicId)
}

data class CommentChildrenDTO(
    val children: List<CommentDataDTO>
) : RawCommentsDTO {
    override fun toCommentsDTO(topicId: String): List<CommentDTO> = children.map{it.toCommentDTO(topicId)}
}

interface RawCommentsDTO {
    fun toCommentsDTO(topicId: String) : List<CommentDTO>
}

interface RawCommentT {
    fun toCommentDTO(topicId: String) : CommentDTO
}

data class RawCommentT1DTO(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val body: String,
    val author: String,
    val replies: RedditDataDTO?,
    val id: String,
) : RawCommentT {
    override fun toCommentDTO(topicId: String): CommentDTO = CommentDTO(
        created, ups, downs, body, author, replies?.toCommentsDTO(topicId) ?: emptyList(), id, topicId
    )
}
data class RawCommentT3DTO(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val selftext: String?,
    val author: String,
    val id: String,
) : RawCommentT{
    override fun toCommentDTO(topicId: String): CommentDTO = CommentDTO(
        created, ups, downs, selftext ?: "", author, emptyList(), id, topicId
    )
}
data class CommentsSnapshot(
    val comments: List<CommentDTO>,
    val date: Date = Date()
)

interface Listable {
    fun toListComment() : ListComment
}
data class ListComment(
    val created: Long,
    val ups: Int,
    val downs: Int,
    val text: String,
    val author: String,
    val id: String,
    val topicId: String
)
data class TopicAboutDTO(
    @JsonProperty("created_utc")
    val created: Long,
    @JsonProperty("subscribers")
    val subscribers: Long,
    @JsonProperty("public_description")
    val description: String
)

open class TopicInfoDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("author_fullname")
    val author: String,
    @JsonProperty("created")
    val created: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("selftext")
    val text: String,
    @JsonProperty("selftext_html")
    val textHtml: String?,
    @JsonProperty("ups")
    val ups: Int,
    @JsonProperty("downs")
    val downs: Int
)

data class RawTopicInfoDTO(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("author_fullname")
    val author: String,
    @JsonProperty("created")
    val created: Long,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("selftext")
    val text: String,
    @JsonProperty("selftext_html")
    val textHtml: String?,
    @JsonProperty("ups")
    val ups: Int,
    @JsonProperty("downs")
    val downs: Int,
    @JsonProperty("permalink")
    val permalink: String
) {
    fun toTopicInfoDTO() : TopicInfoDTO = TopicInfoDTO(id, author, created, title, text, textHtml, ups, downs)
}


data class TopicSnapshot(
    val about: TopicAboutDTO,
    val infos: List<TopicInfoDTO>,
    val date: Date = Date()
)

data class RedditTopicPageDTO(
    @JsonProperty("data")
    val redditTopicListDTO: RedditTopicListDTO
)

data class RedditTopicListDTO(
    @JsonProperty("children")
    val redditTopicDataDTOs: List<RedditTopicDataDTO>
)

data class RedditTopicDataDTO(
    @JsonProperty("data")
    val rawTopicInfoDTO: RawTopicInfoDTO
)

data class TopicWithCommentsSnapshots(
    val topicSnapshot: TopicSnapshot,
    val commentsSnapshot: List<CommentsSnapshot>
)

