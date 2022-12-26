package homework03

data class TopicSnapshot(
    val creationTime: Long,
    val subscribersOnline: Int,
    val description: String,
    val posts: List<Post>
)

data class Subreddit(val data: SubredditData)

data class SubredditAbout(val data: SubredditAboutData)

data class SubredditAboutData(val subscribers: Int, val created: Long, val description: String)

data class SubredditData(val children: List<Child>)

data class Child(val data: Post)


// todo should be camel case
data class Post(
    val author: String,
    val created: Long,
    val ups: Int,
    val downs: Int,
    val title: String,
    val selftext: String?,
    val id: String
)