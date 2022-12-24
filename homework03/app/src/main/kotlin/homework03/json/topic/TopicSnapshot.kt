package homework03.json.topic

var topicId = 0L

data class TopicSnapshot(
    val creationTime: Double,
    val online: Long,
    val description: String,
    val posts: List<Post>,
    val snapshotTime: Long = System.currentTimeMillis(),
    val id: Long = topicId++
)