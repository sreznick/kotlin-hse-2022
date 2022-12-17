package homework03

import homework03.client.RedditClient
import homework03.csv.CsvWriter
import homework03.csv.csvSerialize
import homework03.json.Comment
import homework03.json.CommentsSnapshot
import homework03.json.TopicSnapshot
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException

fun main(args: Array<String>) = runBlocking {
    if (args.size % 2 == 0 || args.size < 3) {
        throw IllegalArgumentException("Number of program arguments must be odd and greater than 3 " +
                "(path for files and then pairs of topic name and comment title)")
    }
    val path = args[0]

    val topicJobs = arrayListOf<Deferred<TopicSnapshot>>()
    val commentJobs = arrayListOf<Deferred<CommentsSnapshot>>()
    for (i in 1 until args.size step 2) {
        topicJobs.add(async { RedditClient.getTopic(args[i]) })
        commentJobs.add(async { RedditClient.getComments(args[i], args[i + 1]) })
    }

    val topicsCsv = csvSerialize(topicJobs.awaitAll(), TopicSnapshot::class)
    CsvWriter.write(topicsCsv, path, "--subjects.csv")

    val commentsCsv = csvSerialize(commentJobs.awaitAll().map { it.linearize() }.flatten(), Comment::class)
    CsvWriter.write(commentsCsv, path, "--comments.csv")
}
