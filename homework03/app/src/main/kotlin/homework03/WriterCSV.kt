package homework03

import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.localCurrentDirVfs
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.stream.writeString
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun <T : Any> csvSerialize(data: Iterable<T>, klass: KClass<T>) =
    buildString { serializeObject(data, klass) }

private fun <T : Any> StringBuilder.serializeObject(data: Iterable<T>, klass: KClass<T>) {
    serializeHeader(klass)
    append("\n")

    if (data.any {
            it.javaClass.kotlin != klass
        }) throw IllegalArgumentException("not all types match")

    data.forEach {
        serializeObject(it)
        append("\n")
    }
}

private fun StringBuilder.serializeNumber(value: Number) = apply {
    append(value)
}


private fun StringBuilder.serializeValue(value: Any) = apply {
    val kClass = value.javaClass.kotlin
    when (kClass) {
        String::class -> {
            serializeString(value as String)
        }
        Date::class -> {
            serializeString(value.toString())
        }
        Integer::class, Short::class, Long::class, Byte::class, Float::class, Double::class -> {
            serializeNumber(value as Number)
        }
    }
}

private fun <T : Any> KClass<T>.filteredProperties(): List<String> = when (this) {
    HotSnapshot::class -> listOf("author", "datePublished", "downs", "id", "permalink", "text", "ups")

    CommentBody::class -> listOf("author", "id", "text", "timePublished", "ups")

    Comment::class -> CommentBody::class.filteredProperties().plus("replies")
    CommentSnapshot::class -> listOf("post_id").plus(Comment::class.filteredProperties())
    else -> memberProperties.map { it.toString() }
}

private fun StringBuilder.serializeString(value: String) = apply {
    append('"')
    append(value.replace("\n", " ").replace('"', '\''))
    append('"')
}

private fun <T : Any> StringBuilder.serializeHeader(klass: KClass<T>): StringBuilder = apply {
    append("")
    val properties = klass.filteredProperties()

    when (klass) {
        String::class -> {
            serializeString("value")
        }

        else -> {
            properties.joinTo(this, ",") {
                serializeString(it)
                ""
            }
        }
    }
}


private fun StringBuilder.serializeObject(value: Any) {
    val kClass = value.javaClass.kotlin
    val properties = kClass.memberProperties.filter { kClass.filteredProperties().contains(it.name) }

    when (kClass) {
        String::class -> {
            serializeString(value as String)
        }
        CommentSnapshot::class -> {
            val id = (value as CommentSnapshot).postId
            value.comments.forEach {
                serializeObject(id)
                append(",")
                serializeObject(it)
                append("\n")
            }
        }

        Comment::class -> {
            serializeObject((value as Comment).body)
            append(",").append("\"[")
            value.replies.forEach { append(it) }
            append("]\"")
        }
        else -> {
            properties.joinTo(this, ",") { p ->
                serializeValue(p.get(value) ?: throw IllegalArgumentException())
                ""
            }
        }
    }
}

private suspend fun writeWithCwd(cwd: VfsFile, fileName: String, csv: String) {
    println("Writing to $fileName...")
    val file = cwd[fileName].open(VfsOpenMode.CREATE_OR_TRUNCATE)
    file.writeString(csv)
    file.close()
    println("Successfully written to $fileName")
}

suspend fun writeCsv(path: String, fileName: String, csv: String) {
    writeWithCwd(localVfs(path), fileName, csv)
}


suspend fun writeCsv(fileName: String, csv: String) {
    writeWithCwd(localCurrentDirVfs, fileName, csv)
}
