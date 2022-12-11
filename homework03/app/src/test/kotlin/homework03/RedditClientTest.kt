package homework03

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RedditClientTest {
    @Test
    fun testNonExistent() = runBlocking {
        val args = listOf("Kotlinnnn", "", " ")
        val client = RedditClient()
        for (name in args) {
            assertFailsWith<RedditClientException> {
                client.getTopic(name)
            }
        }
    }
}