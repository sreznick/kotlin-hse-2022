package homework03

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.stream.writeString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun writeCsv(name: String, client: RedditClient) {
    val dir = localCurrentDirVfs
    val skips = listOf("replies")
    val topicSnapshot = client.getTopic(name)
    val topicCsv = dir["$name$subjectsFile"].open(VfsOpenMode.CREATE)
    val commentsCsv = dir["$name$commentsFile"].open(VfsOpenMode.CREATE)
    coroutineScope {
        launch {

            topicCsv.writeString(buildString {
                serializeHeader(Post::class, emptyList())
                append("\n")
            })

            topicCsv.writeString(buildString {
                serializeIterableWithoutHeader(
                    topicSnapshot.posts,
                    Post::class,
                    emptyList()
                )
            })
        }

        commentsCsv.writeString(buildString {
            serializeHeader(CommentsSnapshot::class, skips)
            append("\n")
        })
        topicSnapshot.posts.map { it.id }.forEach {
            launch {
                commentsCsv.writeString(
                    buildString {
                        serializeIterableWithoutHeader(
                            client.getComments(it).linearize(),
                            CommentsSnapshot::class,
                            skips
                        )
                    }
                )
            }
        }

    }
    topicCsv.close()
    commentsCsv.close()
}