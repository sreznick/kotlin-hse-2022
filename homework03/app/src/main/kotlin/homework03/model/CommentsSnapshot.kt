package homework03.model

data class CommentsSnapshot(val comments: List<RedditComment>) {
    val timestamp = System.currentTimeMillis()
}
