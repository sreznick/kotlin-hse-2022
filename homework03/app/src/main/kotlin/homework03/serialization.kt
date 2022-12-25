package homework03

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun <T: Any> serialize(data: Iterable<T>, klass: KClass<T>) = buildString { serializeObject(data, klass) }

private fun <T: Any> StringBuilder.serializeObject(data: Iterable<T>, klass: KClass<T>) {
    serializeHeader(klass)
    append("\n")

    if (data.any {
            it.javaClass.kotlin != klass
        }) throw IllegalArgumentException("types mismatch")

    data.forEach {
        serializeObject(it)
        append("\n")
    }
}

private fun StringBuilder.serializeNumber(value: Number) = apply {
    append(value)
}

private fun StringBuilder.serializeValue(value: Any) = apply {
    val klass = value.javaClass.kotlin
    when (klass) {
        String::class -> {
            serializeString(value as String)
        }
        Integer::class, Short::class, Long::class, Byte::class, Float::class, Double::class -> {
            serializeNumber(value as Number)
        }
    }
}

private fun StringBuilder.serializeString(value: String) = apply {
    append('"')
    append(value)
    append('"')
}

private fun <T: Any> StringBuilder.serializeHeader(klass: KClass<T>) = apply {
    append("")
    val props = klass.memberProperties

    when (klass) {
        String::class -> {
            serializeString("value")
        }
        else -> {
            props.joinTo(this, ",") { p ->
                serializeString(p.name)
                ""
            }
        }
    }
}

private fun StringBuilder.serializeObject(value: Any) {
    val klass = value.javaClass.kotlin
    val props = klass.memberProperties

    when (klass) {
        String::class -> {
            serializeString(value as String)
        }
        else -> {
            props.joinTo(this, ",") { p ->
                serializeValue(p.get(value) ?: throw IllegalArgumentException())
                ""
            }
        }
    }
}