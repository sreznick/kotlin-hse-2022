import java.io.File

data class Location(val input: StringView, val offset: Int = 0) {
    private val slice by lazy { input.toString().slice(0..offset + 1) }
    val line by lazy { slice.count { it == '\n' } + 1 }
    val column by lazy {
        when (val n = slice.lastIndexOf('\n')) {
            -1 -> offset + 1
            else -> offset - n
        }
    }
}

sealed class Result<A : Any>
data class Success<A : Any>(val value: A, val consumed: Int) : Result<A>()
data class Failure<A : Any>(val get: ParseError) : Result<A>()

data class ParseError(val stack: List<Pair<Location, String>>)

typealias Parser<A> = (StringView) -> Result<A>

internal fun Location.toError(msg: String) = ParseError(listOf(this to msg))

class StringView(
    private val string: String, private val offset: Int = 0, private val length: Int = string.length - offset
) {
    fun get(index: Int) = string[offset + index]
    fun startsWith(c: Char) = length > 0 && string[offset] == c
    fun length() = length
    fun substring(start: Int, newLength: Int = length - start) =
        StringView(string, offset + start, minOf(newLength, length - start))

    override fun toString() = string.substring(offset, offset + length)
}

interface CharParsers {
    fun notChar(c: Char): Parser<Char> = { input: StringView ->
        if (input.length() > 0 && input.get(0) != c) Success(input.get(0), 1)
        else Failure(Location(input).toError("Not expected: $c"))
    }
}

interface StringParsers {
    fun string(s: String): Parser<String> = { input: StringView ->
        if (input.substring(0, s.length).toString() == s) Success(s, s.length)
        else Failure(Location(input).toError("Expected: $s"))
    }
}

interface Combinators {
    fun <T : Any> or(some: Parser<T>, other: Parser<T>): Parser<T> = { input: StringView ->
        when (val someResult = some(input)) {
            is Failure -> other(input)
            else -> someResult
        }
    }

    fun <A : Any, B : Any, R : Any> seq(first: Parser<A>, second: Parser<B>, merge: (A, B) -> R): Parser<R> =
        { input: StringView ->
            when (val firstValue = first(input)) {
                is Failure -> Failure(firstValue.get)
                is Success -> when (val secondValue = second(input.substring(firstValue.consumed))) {
                    is Failure -> Failure(secondValue.get)
                    is Success -> Success(
                        merge(firstValue.value, secondValue.value), firstValue.consumed + secondValue.consumed
                    )
                }
            }
        }

    fun <A : Any, B : Any, C : Any, R : Any> seq(
        first: Parser<A>, second: Parser<B>, third: Parser<C>, merge: (A, B, C) -> R
    ): Parser<R> = { input: StringView ->
        when (val firstValue = first(input)) {
            is Failure -> Failure(firstValue.get)
            is Success -> when (val secondValue = second(input.substring(firstValue.consumed))) {
                is Failure -> Failure(secondValue.get)
                is Success -> when (val thirdValue =
                    third(input.substring(firstValue.consumed + secondValue.consumed))) {
                    is Failure -> Failure(thirdValue.get)
                    is Success -> Success(
                        merge(firstValue.value, secondValue.value, thirdValue.value),
                        firstValue.consumed + secondValue.consumed + thirdValue.consumed
                    )
                }
            }
        }
    }

    fun <T : Any> repeatWhileSuccessNotZero(some: Parser<T>): Parser<List<T>> = { input: StringView ->
        run {
            val resultList = mutableListOf<T>()
            var consumedSum = 0
            repeat(input.length() + 1) {
                return@repeat when (val parsed = some(input.substring(consumedSum))) {
                    is Failure -> {
                        if (resultList.size == 0) {
                            return@run Failure(parsed.get)
                        }
                        return@run Success(resultList, consumedSum)
                    }

                    is Success -> {
                        resultList.add(parsed.value)
                        consumedSum += parsed.consumed
                    }
                }
            }
            Success(resultList, consumedSum)
        }
    }

    fun <T : Any> repeatWhileSuccess(some: Parser<T>): Parser<List<T>> = { input: StringView ->
        when (val parsed = repeatWhileSuccessNotZero(some)(input)) {
            is Failure -> Success(listOf(), 0)
            is Success -> parsed
        }
    }

    fun <A : Any, B : Any> changeResult(some: Parser<A>, changer: (A) -> B): Parser<B> = { input: StringView ->
        when (val parsed = some(input)) {
            is Failure -> Failure(parsed.get)
            is Success -> Success(changer(parsed.value), parsed.consumed)
        }
    }
}

class HTMLParser : Combinators, CharParsers, StringParsers {
    private val parseNothing = string("")

    private val textParser = changeResult(repeatWhileSuccessNotZero(notChar('<'))) { it.joinToString("") }

    @JvmName("anotherOr")
    private infix fun <T : Any> Parser<T>.or(other: Parser<T>) = or(this, other)

    private val pParser: Parser<Internal> =
        seq(string("<p>"), (textParser or parseNothing), (string("</p>") or parseNothing)) { _, b, _ -> P(b) }

    private val divParser: Parser<Internal> = seq(
        string("<div>"), repeatWhileSuccess { internalParser()(it) }, (string("</div>") or parseNothing)
    ) { _, b, _ -> Div(b) }

    private fun internalParser(): Parser<Internal> = divParser or pParser or changeResult(textParser) { P(it) }

    private val bodyParser = seq(
        string("<body>"), repeatWhileSuccess(internalParser()), (string("</body>") or parseNothing)
    ) { _, b, _ -> Body(b) }

    private val htmlParser =
        changeResult(bodyParser or changeResult(repeatWhileSuccess(internalParser())) { Body(it) }) { HTML(it) }

    fun parse(input: String): Result<HTML> {
        return htmlParser(StringView(input))
    }
}

data class HTML(val body: Body) {
    override fun toString(): String {
        return body.toString()
    }
}

data class Body(val internals: List<Internal>) {
    override fun toString(): String {
        return "<body>${internals.fold("") { a, b -> a + b.toString() }}</body>"
    }
}

sealed class Internal
data class P(val text: String) : Internal() {
    override fun toString(): String {
        return "<p>${text}</p>"
    }
}

data class Div(val internals: List<Internal>) : Internal() {
    override fun toString(): String {
        return "<div>${internals.fold("") { a, b -> a + b.toString() }}</div>"
    }
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Enter two files")
        return
    }
    val input = File(args[0])
    if (!input.isFile) {
        println("No such file: ${args[0]}")
        return
    }
    when (val parsedHTML = HTMLParser().parse(input.readText())) {
        is Failure -> println("Some error while parsing: ${parsedHTML.get}")
        is Success -> {
            println("Successfully parsed")
            val output = File(args[1])
            output.writeText(parsedHTML.value.toString())
        }
    }
}
