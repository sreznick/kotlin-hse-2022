package homework03

import java.net.URL

class URLMapper {

    fun mapToJsonURL(topic: String) : URL {
        return URL("$topic${Config.topicJsonName}")
    }

    fun mapToAboutJsonUrl(topic: String) : URL {
        return URL("$topic${Config.aboutJsonName}")
    }



}