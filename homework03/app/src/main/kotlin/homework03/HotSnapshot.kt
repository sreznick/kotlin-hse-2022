package homework03

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class HotSnapshot(
    val author: String,
    val datePublished: Date,
    val permalink: String,
    val ups: Int,
    val downs: Int,
    val text: String,
    val htmlText: String,
    val id: String
)  {

    companion object {

        @JvmStatic
        @JsonCreator
        fun create(
            author: String, @JsonSetter("created") datePublished: Double,
            permalink: String, ups: Int, upvote_ratio: Float,
            @JsonSetter("selftext") text: String, @JsonSetter("selftext_html") htmlText: String?,
            @JsonSetter("name") id: String)
        = HotSnapshot(author, Date(datePublished.toLong() * 1000),
            "https://www.reddit.com$permalink",
            ups, ups * (1 - 1 / upvote_ratio).toInt(), text, htmlText ?: "", id)
    }
}


