package homework03

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// first parameter - path to save files
suspend fun main(args: Array<String>) = coroutineScope {
    if (args.size > 1) {
        val redditClient = RedditClient()
        val path = args[0]
        for (topicName in args.withIndex()) {
            if (topicName.index > 0) {
                launch { redditClient.getAndSaveTopicInfo(topicName.value, path) }
            }
        }
    }
}
