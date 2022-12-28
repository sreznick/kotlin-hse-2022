package homework03.dto.comment

import homework03.lib.Utils

class CommentsSnapshot(commentsRaw: CommentsJson){
    private val idGenerator = Utils.IdGenerator()
    val comments = commentsRaw.data?.children?.filter { it.data != null }?.map{ Utils.convertCommentsJsonToCommentsSnapshot(it.data!!, null, idGenerator) }
}

class CommentSnapshot(commentData: CommentsJson.CommentJsonData.CommentsData.CommentData, val id: Int, val parentId: Int?, val children: List<CommentSnapshot>?) {
    val creationTime = commentData.created?.value
    val upVotes = commentData.ups
    val downVotes = commentData.downs
    val text = commentData.body
    val textHtml = commentData.body_html
    val author = commentData.author_fullname
    val depth = commentData.depth
}

data class LinearComment(val creationTime: Double?, val upVotes: Int?, val downVotes: Int?, val text: String?, val author: String?, val id: Int, val parentId: Int?, val discussionId: String?)

