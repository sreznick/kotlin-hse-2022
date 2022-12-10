package homework03

import com.soywiz.korio.net.http.createHttpClient
import homework03.dto.comment.CommentsSnapshot
import homework03.dto.topic.TopicSnapshot
import homework03.lib.Utils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RedditClient {
    private val httpClient = createHttpClient();

    fun generateLinkByPermlink(suffix: String): String {
        return "https://www.reddit.com${suffix}.json"
    }

    suspend fun getTopic(name: String): TopicSnapshot = coroutineScope {
        val topicMainAsync = async {
            val res = httpClient.readBytes("https://www.reddit.com/r/${name}/.json")
            Utils.parseTopicMain(Utils.ungzip(res));
        }
        val topicAboutAsync = async {
            val res = httpClient.readBytes("https://www.reddit.com/r/${name}/about.json")
            Utils.parseTopicAbout(Utils.ungzip(res));
        }
        val topicMain = topicMainAsync.await();
        val topicAbout = topicAboutAsync.await();
        val timeStamp = System.currentTimeMillis().toDouble();
        TopicSnapshot(topicMain, topicAbout, timeStamp)
    }


    suspend fun getComments(permlink: String): CommentsSnapshot? = coroutineScope {
        val commentsMainAsync = async {
            val res = httpClient.readBytes(generateLinkByPermlink(permlink));
            Utils.parseComments(Utils.ungzip(res));
        }
        val commentsMain = commentsMainAsync.await();
        val comments = commentsMain.getOrNull(1);
        if (comments == null) {
            null
        } else {
            CommentsSnapshot(comments)
        }
    }
}