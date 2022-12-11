package homework03.json

import homework03.serializer.CsvIgnore

internal data class TopicSnapshot(
    val creationTime: Long,
    val subscribersOnline: Int,
    val description: String,
    @CsvIgnore
    val reachableDiscussionsInfo: List<DiscussionInformation>,
    val id: String
) {
    val requestTime = System.currentTimeMillis() / 1000

}