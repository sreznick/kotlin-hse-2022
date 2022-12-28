package homework04.parseUtils

class StringLocation(override val input: StringView, override val offset: Int = 0) : Location<StringView> {
    private val slice by lazy { input.slice(0..offset + 1) }
    val line by lazy { slice.count { it == '\n' } + 1 }
    val column by lazy {
        when (val n = slice.lastIndexOf('\n')) {
            -1 -> offset + 1
            else -> offset - n
        }
    }
}

interface StringParsers {
    fun string(s: String): Parser<StringView, String> = { input: StringView ->
        if (input.startsWith(s))
            Success(s, s.length)
        else Failure(StringLocation(input).toError("Expected: $s"))
    }

    fun nothing(): Parser<StringView, String> = { Success("", 0) }
}