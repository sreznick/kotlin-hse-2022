package homework03.model.json

data class TopicAboutJsonModel(val data: TopicSnapshotAbout)

data class TopicSnapshotAbout(
    val id: String,
    val created: Int,
    val subreddit: String?,
    val subscribers: Int,
    val description: String) {

}
