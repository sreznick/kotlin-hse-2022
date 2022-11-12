package homework03.model.json

import com.fasterxml.jackson.annotation.*

data class CommentsJsonModel(
    val data: CommentsJsonModelData
)

data class CommentsJsonModelData(
    val children: List<CommentInfoChildKind>
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes(
    JsonSubTypes.Type(CommentInfoChildKind1::class, name = "t1"),
    JsonSubTypes.Type(CommentInfoChildKind3::class, name = "t3"),
    JsonSubTypes.Type(CommentInfoChildKindMore::class, name = "more")
)
open class CommentInfoChildKind(val kind: String)
data class CommentInfoChildKind1(val data: RedditCommentJson) : CommentInfoChildKind("t1")
data class CommentInfoChildKind3(val data: RedditThread) : CommentInfoChildKind("t3")
data class CommentInfoChildKindMore(val data: RedditMoreComments) : CommentInfoChildKind("more")
data class RedditCommentJson(
    val created: Long,
    val score: Int,
    @JsonProperty("body")
    val text: String,
    val replies: CommentsJsonModel?,
    val author: String,
    val id: String,
    val depth: Int
) {
}

data class RedditThread(
    val title: String,
    val author: String,
    val created: Int,
    val score: Int,
    val permalink: String,
    val id: String,
    @JsonProperty("selftext")
    val text: String,
    @JsonProperty("selftext_html")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val textHtml: String
) {
}

data class RedditMoreComments(
    val count: Int,
    val id: String,
    val children: List<String>
)