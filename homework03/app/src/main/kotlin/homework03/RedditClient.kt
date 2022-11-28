package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

object RedditClient {
    private val httpClient = HttpClient(CIO)
    private val objectMapper = ObjectMapper()

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    suspend fun getTopic(name: String): TopicSnapshot {
        val aboutJson = httpClient.get("https://www.reddit.com/r/$name/about.json").body<String>()
        val topicMainData = objectMapper.readValue(aboutJson, JsonTopicInfoWrapper::class.java)
        val postsJson = httpClient.get("https://www.reddit.com/r/$name/.json").body<String>()
        val posts = objectMapper.readValue(postsJson, JsonPostsWrapper::class.java)
        return TopicSnapshot.create(topicMainData, posts)
    }
}