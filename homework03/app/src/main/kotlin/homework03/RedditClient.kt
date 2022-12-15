package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.cfg.CoercionAction
import com.fasterxml.jackson.databind.cfg.CoercionInputShape
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*


object RedditClient {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                coercionConfigFor(CommentInfo::class.java)
                    .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull)
            }
        }
    }

    suspend fun getTopic(name: String): TopicSnapshot {
        val about: SubredditAbout = httpClient.get("https://www.reddit.com/r/$name/about.json").body()
        val posts: Subreddit = httpClient.get("https://www.reddit.com/r/$name/.json").body()

        return TopicSnapshot(about.data.created,
            about.data.subscribers,
            about.data.description,
            posts.data.children.map { it.data })
    }

    private fun mapToSnapshot(info: CommentInfo?, id: Int, depth: Int): Pair<Int, List<CommentsSnapshot>> {
        if (info == null) {
            return Pair(id, emptyList())
        }
        val replies = mutableListOf<CommentsSnapshot>()
        var myId = id
        info.data.children.forEach { kind ->
            when (kind) {
                is CommentInfoChildKind1 -> {
                    val pair = mapToSnapshot(kind.data.replies, myId, depth + 1)
                    myId = pair.first
                    replies.add(
                        CommentsSnapshot(
                            kind.data.created_utc,
                            kind.data.ups,
                            kind.data.downs,
                            kind.data.body,
                            kind.data.author,
                            -1,
                            pair.second,
                            depth,
                            myId
                        )
                    )
                    myId++
                }
                else -> {
                    throw IllegalArgumentException("Only t1 is allowed here")
                }
            }
        }
        return Pair(myId, replies)
    }

    private fun setParent(snapshot: CommentsSnapshot, parent: Int) {
        snapshot.replyTo = parent
        snapshot.replies.forEach {
            setParent(it, snapshot.id)
        }
    }

    suspend fun getComments(topicName: String, title: String): CommentsSnapshot {
        val info: List<CommentInfo> = httpClient.get("https://www.reddit.com/r/$topicName/comments/$title/.json").body()
        val main = (info[0].data.children[0] as CommentInfoChildKind3).data
        val pair = mapToSnapshot(info[1], 0, 1)
        val snap = CommentsSnapshot(
            main.created,
            main.ups,
            main.downs,
            main.selftext,
            main.author,
            -1,
            pair.second,
            0,
            pair.first
        )
        setParent(snap, -1)
        return snap
    }
}