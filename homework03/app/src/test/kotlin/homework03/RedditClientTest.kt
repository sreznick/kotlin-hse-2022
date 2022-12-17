package homework03

import homework03.client.RedditClient
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RedditClientTest {

    @Test
    fun getTopic_Kotlin() = runBlocking {
        val topic = RedditClient.getTopic("Kotlin")
        println(topic)
    }

    @Test
    fun getTopic_Python() = runBlocking {
        val topic = RedditClient.getTopic("Python")
        println(topic)
    }

    @Test
    fun getComments_Kotlin() = runBlocking {
        val commentsSnapshot = RedditClient.getComments("Kotlin", "z3qwxa/additional_monads_not_defined_in_arrow")
        println(commentsSnapshot)
    }
}