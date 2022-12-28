package homework03.dto.topic

class TopicSnapshot (topicMain: TopicMain, topicAbout: TopicAbout, val requestTime: Double) {
    val creationTime = topicAbout.data?.created?.value;
    val onlineSubscribers = topicAbout.data?.active_user_count;
    val description = topicAbout.data?.description;

    val discussions = topicMain.data?.children?.map { TopicDiscussion(it) };

    class TopicDiscussion (discussion: TopicMain.TopicMainData.TopicMainDataChild){
        val author = discussion.data?.author_fullname
        val createdTime = discussion.data?.created?.value
        val upVotes = discussion.data?.ups
        val downVotes = discussion.data?.downs
        val title = discussion.data?.title
        val text = discussion.data?.selftext
        val textHtml = discussion.data?.selftext_html
        val id = discussion.data?.id
        val permalink = discussion.data?.permalink
    }
}