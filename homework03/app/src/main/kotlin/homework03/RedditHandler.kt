package homework03

import homework03.model.CommentsSnapshot
import homework03.model.TopicSnapshot
import homework03.model.TopicWithCommentsSnapshots

interface RedditHandler {
    suspend fun getComments(title: String): CommentsSnapshot
    suspend fun getTopic(title: String): TopicSnapshot

    suspend fun getTopicWithComments(title: String): TopicWithCommentsSnapshots

}