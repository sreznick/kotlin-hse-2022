package homework03


import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope


fun main(args: Array<String>) = runBlocking {
    val client = RedditClient()
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Cant create file; got: $exception")
    }

    supervisorScope {
        for (topicName in args) {
            launch(handler) {
                val topic = client.getTopic(topicName)
                CsvFileWriter.writeTopicAndComments(topic, topicName, client)
            }
        }
    }

}

