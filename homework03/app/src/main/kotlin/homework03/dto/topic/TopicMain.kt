package homework03.dto.topic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import homework03.dto.Created

@JsonIgnoreProperties(ignoreUnknown = true)
class TopicMain {
    val data: TopicMainData? = null

    @JsonIgnoreProperties(ignoreUnknown = true)
    class TopicMainData {
        val children: List<TopicMainDataChild>? = null

        @JsonIgnoreProperties(ignoreUnknown = true)
        class TopicMainDataChild{
            val data: TopicMainDataChildData? = null


            @JsonIgnoreProperties(ignoreUnknown = true)
            class TopicMainDataChildData{
                val id: String? = null
                val ups: Int? = null
                val downs: Int? = null
                val title: String? = null
                val selftext: String? = null
                val selftext_html: String? = null
                val author_fullname: String? = null
                val created: Created? = null
                val permalink: String? = null
            }
        }
    }


}







