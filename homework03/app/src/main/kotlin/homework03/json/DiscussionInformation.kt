package homework03.json

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonNaming
import homework03.serializer.CsvIgnore

@JsonIgnoreProperties(ignoreUnknown = true)
data class DiscussionInformation(
    @JsonProperty("author")
    val messageAuthor: String,
    @JsonProperty("created")
    val publicationTime: Long,
    val ups: Int,
    val downs: Int,
    @JsonProperty("title")
    val header: String,
    @JsonProperty("selftext")
    val text: String?,
//    @CsvIgnore
    @JsonProperty("selftext_html")
    val htmlText: String?,
    val id: String
)
