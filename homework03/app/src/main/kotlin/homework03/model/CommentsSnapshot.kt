package homework03.model

import java.util.*
import kotlin.collections.ArrayList

data class CommentsSnapshot(
    val comments: List<CommentDTO>,
    val date: Date
) {

    private fun List<CommentDTO>.flatten(list: MutableList<CommentDTO>) : List<CommentDTO> {
        if (this.isEmpty())
            return list
        this.map {
            list.add(it)
            it.children.flatten(list)
        }
        return list
    }
    private fun List<CommentDTO>.flatten() : List<CommentDTO> = this.flatten(ArrayList())
    fun flatten() : CommentsSnapshot = CommentsSnapshot(
        this.comments.flatten(),
        this.date
    )
}