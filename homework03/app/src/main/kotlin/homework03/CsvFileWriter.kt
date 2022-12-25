package homework03

import homework03.json.DiscussionInformation
import homework03.json.SingleCommentSnapshot
import homework03.serializer.csvSerialize
import homework03.serializer.csvSerializeHeader
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString
import homework03.json.TopicSnapshot

internal class CsvFileWriter {
    companion object {
        suspend fun writeTopicAndComments(topic: TopicSnapshot, name: String, client: RedditClient) {

            val cwd = localCurrentDirVfs
            val topicFile = cwd["$name-${topic.requestTime}-subjects.csv"].open(VfsOpenMode.CREATE)
            val commentsFile = cwd["$name-${topic.requestTime}-comments.csv"].open(VfsOpenMode.CREATE)
            coroutineScope {
                launch {
                    topicFile.writeString(csvSerializeHeader(DiscussionInformation::class))
                    topicFile.writeString(csvSerialize(topic.reachableDiscussionsInfo, DiscussionInformation::class))

                }
                val reachableDiscussionsIds = topic.reachableDiscussionsInfo.map { it.id }
                commentsFile.writeString(csvSerializeHeader(SingleCommentSnapshot::class))
                for (commentId in reachableDiscussionsIds) {
                    launch {
                        val comment = client.getComments(commentId)
                        commentsFile.writeString(csvSerialize(comment.comments, SingleCommentSnapshot::class))
                    }
                }
            }
            topicFile.close()
            commentsFile.close()

        }
    }
}