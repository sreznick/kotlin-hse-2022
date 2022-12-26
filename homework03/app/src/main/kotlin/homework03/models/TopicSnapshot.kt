package homework03.models

import homework03.networkingDTO.topic.PostDTO

data class TopicSnapshot(
        val creationTime: Double,
        val subscribersOnline: Long,
        val description: String,
        val posts: List<PostDTO>
)
