package homework03

import homework03.json.SingleCommentSnapshot

internal data class CommentsSnapshot(
    val comments: List<SingleCommentSnapshot>
)
