package homework03

import homework03.model.CommentsSnapshot
import homework03.model.TopicSnapshot

interface RedditHandler {
    suspend fun getComments(title: String): CommentsSnapshot
    suspend fun getTopic(name: String): TopicSnapshot

}