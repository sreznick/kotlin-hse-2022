package homework03

class URLMapper {

    fun mapToJsonURL(topic: String) : String {
        return "$topic/${Config.topicJsonName}"
    }

    private fun mapToAboutJson(url: String) : String {
        return "$url/about${Config.topicJsonName}"
    }
    fun mapToAboutUrl(topic: String) : String {
        return mapToAboutJson("${Config.subredditStringURL}/$topic")
    }

    fun mapToTopicUrl(topic: String) : String {
        return mapToJsonURL("${Config.subredditStringURL}/$topic")
    }
    fun mapPermalinkToCommentLink(permalink: String) : String {
        return "${Config.redditStringURL}$permalink.json"
    }

}