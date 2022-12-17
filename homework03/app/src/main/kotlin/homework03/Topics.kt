package homework03

import com.fasterxml.jackson.databind.BeanDescription
import java.util.Date

data class Discussion(
    val author: String,
    val publicDate: Date,
    val upVotes: Int,
    val downVotes: Int,
    val title: String,
    val selfText: String,
    val selfHtmlText: String,
    val id: String
)

data class TopicSnapShot(
    val publicDate: Date,
    val onlineSubs: Int,
    val description: String,
    val discussion: List<Discussion>,
    val receivingDate: Date
)
