import parsers.ParseError

data class Location<out T: Any>(val input: T, val offset: Int = 0)

internal fun <T: Any> Location<T>.toError(msg: String) = ParseError(listOf(this to msg))