package homework03

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

private val primitiveTypes = listOf(
    Int::class, Short::class, Long::class, Byte::class, Float::class, Double::class,
    Boolean::class, Char::class
)

fun <T : Any> StringBuilder.serializeIterableWithoutHeader(data: Iterable<T>, klass: KClass<T>, skips: List<String>) {
    if (data.any {
            it.javaClass.kotlin != klass
        }) throw IllegalArgumentException("not all types match")

    data.forEach {
        serializeObject(it, skips)
        append("\n")
    }
}

private fun StringBuilder.serializeSingle(value: Any) = apply { append(value) }

private fun StringBuilder.serializeValue(value: Any?) = apply {
    if (value == null) {
        append("null")
        return this
    }
    val kClass = value.javaClass.kotlin
    when (kClass) {
        String::class -> serializeString(value as String)
        else -> serializeSingle(value)
    }
}

private fun StringBuilder.serializeString(value: String) = apply {
    append('"').append(value).append('"')
}

fun <T : Any> StringBuilder.serializeHeader(klass: KClass<T>, skips: List<String>) = apply {
    append("")
    val properties = klass.memberProperties.filter { it.name !in skips }

    when (klass) {
        String::class, in primitiveTypes -> serializeString("value")
        else -> {
            properties.joinTo(this, ",") { p ->
                serializeString(p.name)
                ""
            }
        }
    }
}

private fun StringBuilder.serializeObject(value: Any, skip: List<String>) {
    val kClass = value.javaClass.kotlin
    val properties = kClass.memberProperties.filter { it.name !in skip }

    when (kClass) {
        String::class -> serializeString(value as String)
        in primitiveTypes -> serializeSingle(value)
        else -> {
            properties.joinTo(this, ",") { p ->
                serializeValue(p.get(value))
                ""
            }
        }
    }
}