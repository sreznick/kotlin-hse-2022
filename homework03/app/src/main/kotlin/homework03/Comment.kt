package homework03

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.Serializable
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentBody(
    val timePublished: Date,
    val id: String,
    val replyTo: String,
    val ups: Int,
    val downs: Int,
    val text: String,
    val author: String,
    val depth: Int
) {

    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            @JsonSetter("created") timePublished: Double, id: String,
            @JsonSetter("parent_id") replyTo: String, ups: Int, downs: Int,
            @JsonSetter("body") text: String?, author: String?, depth: Int
        ) = CommentBody(Date(timePublished.toLong() * 1000), id, replyTo, ups, downs, text ?: "", author ?: "", depth)
    }
}

@JsonDeserialize(using = CommentDeserializer::class)
data class Comment(
    val body: CommentBody,
    val replies: List<Comment>
) {

    // Представление в линейной форме
    override fun toString(): String {
        val rep = replies.joinToString(
            separator = ", ",
            prefix = "[",
            postfix = "]"
        ) { it.toString() }
        return "[${body.author}, ${body.id}, ${body.replyTo}, ${body.ups}, " +
                "${body.text.replace("[\r\n]".toRegex(), " ")}]" + if (rep != "[]") " -> $rep" else ""
    }
}

class CommentDeserializer : JsonDeserializer<Comment>() {
    companion object {
        @JvmStatic
        private val mapper = jacksonObjectMapper()
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): Comment {
        val data: JsonNode = p.readValueAsTree<JsonNode>().get("data")
        var list = listOf<Comment>()
        val hasReplies = data.has("replies") && data.get("replies").has("data")
        if (hasReplies) {
            val children = data.get("replies").get("data").get("children").toString()
            list = mapper.readValue(children)
        }
        return Comment(mapper.readValue(data.toString()), list)
    }

}

