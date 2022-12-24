package homework03.json.comment

data class CommentsSnapshot(val comments: List<Comment>) {
    fun linearize() : List<Comment> {
        val line: MutableList<Comment> = arrayListOf()
        fun rec(comment: Comment) {
            line.add(comment)
            for (reply in comment.replies) {
                rec(reply)
            }
        }
        comments.forEach(::rec)
        return line
    }
}