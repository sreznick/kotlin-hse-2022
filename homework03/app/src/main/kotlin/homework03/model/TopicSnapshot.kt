package homework03.model

import homework03.model.json.RedditThread
import homework03.model.json.TopicSnapshotAbout

data class TopicSnapshot(
    val about: TopicSnapshotAbout,
    val threads: List<RedditThread>
) {
    val timestamp = System.currentTimeMillis()

}
