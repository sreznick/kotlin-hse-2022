package homework03

class Config private constructor() {
    companion object {
        const val redditStringURL = "https://www.reddit.com"
        const val subredditStringURL = "$redditStringURL/r";
        const val aboutJsonName = "about.json"
        const val topicJsonName = ".json"
    }
}