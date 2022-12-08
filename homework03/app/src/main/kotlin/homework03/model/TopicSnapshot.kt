package homework03.model

import java.util.*

data class TopicSnapshot(
    val about: TopicAboutDTO,
    val infos: List<TopicInfoDTO>,
    val date: Date
)