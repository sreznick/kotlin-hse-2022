package homework03

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import java.math.BigDecimal

object CsvSerializer {
    private val basedObject = setOf(
        Integer::class, Short::class,
        Long::class, Byte::class,
        Float::class, Double::class,
        BigDecimal::class
    )

    fun <T : Any> csvSerialize(data: Iterable<T>, klass: KClass<T>) = buildString {
        serializeObject(data, klass)
    }

    private fun <T : Any> StringBuilder.serializeObject(data: Iterable<T>?, klass: KClass<T>) {
        if (data == null) append("null")
        else {
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
    }

    private fun StringBuilder.serializeBased(value: Any) = apply {
        append(value.toString())
    }

    private fun StringBuilder.serializeValue(value: Any?) = apply {
        if (value == null) append("null")
        else when (value.javaClass.kotlin) {
            String::class -> serializeString(value as String)
            in basedObject -> serializeBased(value as Number)
        }
    }

    private fun StringBuilder.serializeString(value: String) = apply {
        append('"')
        append(value)
        append('"')
    }

    private fun <T : Any> StringBuilder.serializeHeader(klass: KClass<T>) = apply {
        append("")
        val properties = klass.memberProperties

        when (klass) {
            String::class, in basedObject -> serializeString("value")
            else -> properties.joinTo(this, ",") { p ->
                serializeString(p.name)
                ""
            }
        }
    }

    private fun StringBuilder.serializeObject(value: Any) {
        val kClass = value.javaClass.kotlin
        val properties = kClass.memberProperties

        when (kClass) {
            String::class -> serializeString(value as String)
            in basedObject -> serializeBased(value)
            else -> properties.joinTo(this, ",") { p ->
                serializeValue(p.get(value))
                ""
            }
        }
    }
}