package homework03

import com.soywiz.korio.lang.substr
import com.soywiz.korio.net.http.Http
import com.soywiz.korio.net.http.createHttpClient
import homework03.dto.comment.CommentsSnapshot
import homework03.dto.topic.TopicSnapshot
import homework03.lib.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URLEncoder

class RedditClient {
    private val httpClient = createHttpClient()

    private fun generateLinkByPermLink(suffix: String): String {
        val preparedSuffix = URLEncoder.encode(
            if (suffix.isNotEmpty() && suffix[0] == '/') {
                suffix.substr(1)
            } else {
                suffix
            }, Charsets.UTF_8
        )
        return "https://www.reddit.com/${preparedSuffix}.json"
    }

    suspend fun getTopic(name: String): TopicSnapshot = coroutineScope {
        val topicMainAsync = async {
            val res = httpClient.readBytes("https://www.reddit.com/r/${name}/.json")
            Utils.parseTopicMain(Utils.ungzip(res))
        }
        val topicAboutAsync = async {
            val res = httpClient.readBytes("https://www.reddit.com/r/${name}/about.json")
            Utils.parseTopicAbout(Utils.ungzip(res))
        }
        val topicMain = topicMainAsync.await()
        val topicAbout = topicAboutAsync.await()
        val timeStamp = System.currentTimeMillis().toDouble()
        TopicSnapshot(topicMain, topicAbout, timeStamp)
    }


    suspend fun getComments(permLink: String): CommentsSnapshot? = coroutineScope {
        val commentsMainAsync = async {
            val res = httpClient.readBytes(generateLinkByPermLink(permLink))
            Utils.parseComments(Utils.ungzip(res))
        }
        try {
            val commentsMain = commentsMainAsync.await()
            val comments = commentsMain.getOrNull(1)
            if (comments == null) {
                null
            } else {
                CommentsSnapshot(comments)
            }
        } catch (e: Http.HttpException) {
            println(generateLinkByPermLink(permLink) == "https://www.reddit.com/r/Pokimane/comments/y4qikp/daydreamin_ω_poki_n_aria_3/.json")
            throw e;
        }
    }
}