package homework03.dto.topic

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import homework03.dto.Created

@JsonIgnoreProperties(ignoreUnknown = true)
data class TopicAbout (val data: TopicAboutData? = null)


@JsonIgnoreProperties(ignoreUnknown = true)
class TopicAboutData{
    val created: Created? = null
    val active_user_count: Int? = null
    val description: String? = null
}