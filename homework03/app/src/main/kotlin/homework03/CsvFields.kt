//package homework03
//
//import java.util.*
//import kotlin.reflect.KClass
//
//// unfortunately Kotlin doensn't allow static interface inheritance, so I had to make this.
//class CsvFields {
//    companion object {
//        @JvmStatic
//        val commentFields = listOf("author", "id", "replyTo", "timePublished",
//            "ups", "downs", "text", "depth", "replies")
//        @JvmStatic
//        val hotSnapshotFields = listOf("author", "datePublished",
//            "downs", "text", "htmlText", "id", "ups")
//        @JvmStatic
//        fun<T: Any> getField(klass: KClass<T>) = when(klass) {
//            HotSnapshot::class -> {
//                hotSnapshotFields
//            }
//            Comment::class -> {
//                commentFields
//            }
//            else -> throw UnsupportedOperationException("Only fields for comment and hotspanshot are supported")
//        }
//    }
//}