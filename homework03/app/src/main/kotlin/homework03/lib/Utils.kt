package homework03.lib

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import homework03.dto.comment.CommentSnapshot
import homework03.dto.comment.CommentsJson
import homework03.dto.comment.LinearComment
import homework03.dto.topic.TopicAbout
import homework03.dto.topic.TopicMain
import homework03.dto.topic.TopicSnapshot
import java.util.zip.GZIPInputStream

class Utils {
    companion object {
        fun ungzip(content: ByteArray): String =
            GZIPInputStream(content.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }

        fun parseTopicMain(content: String): TopicMain = ObjectMapper().readValue(content, TopicMain::class.java)
        fun parseTopicAbout(content: String): TopicAbout = ObjectMapper().readValue(content, TopicAbout::class.java);

        //Микробага в json структуре '"replies": "",' с сервера, много копипасты ради ее исправления писать не хочу
        fun parseComments(content: String): List<CommentsJson> =
            ObjectMapper().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .readValue(content, object : TypeReference<List<CommentsJson>>() {});

        fun convertCommentsJsonToCommentsSnapshot(
            commentData: CommentsJson.CommentJsonData.CommentsData.CommentData,
            parentId: Int?,
            idGenerator: IdGenerator = IdGenerator(1),
        ): CommentSnapshot {
            val currentId = idGenerator.generate();
            val children = commentData.replies?.data?.children?.filter { child -> child.data != null }
                ?.map { child ->
                    convertCommentsJsonToCommentsSnapshot(
                        child.data!!,
                        currentId,
                        idGenerator
                    )
                }
            return CommentSnapshot(commentData, currentId, parentId, children);
        }

        fun getLinear(commentSnapshotList: List<CommentSnapshot>?, discussionId: String?): List<LinearComment> {
            val res: MutableList<LinearComment> = mutableListOf();
            commentSnapshotList?.forEach { res.add(toLinearComment(it, discussionId)); res.addAll(getLinear(it.children, discussionId)) }
            return res;
        }

        fun toLinearComment(it: CommentSnapshot, discussionId: String?): LinearComment =
            LinearComment(it.creationTime, it.upVotes, it.downVotes, it.text, it.author, it.id, it.parentId, discussionId)

        fun getDiscussionLinks(topicSnapshot: TopicSnapshot, discussionIdGenerator: IdGenerator): List<Pair<String?, String>>? =
            topicSnapshot.discussions?.filter { d -> d.permalink != null }?.map { d -> Pair(d.id, d.permalink!!) };
    }

    class IdGenerator(private var id: Int = 1) {
        fun generate(): Int {
            id += 1;
            return id - 1;
        }
    }
}