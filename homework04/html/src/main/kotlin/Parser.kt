sealed class Either<out L : Any, out R : Any> private constructor(left: Any?, right: Any?) {
    abstract fun <T : Any> mapLeft(f: (L) -> T): Left<T>
    abstract fun <T : Any> mapRight(f: (R) -> T): Right<T>

    abstract fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U>
    abstract fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U>
}

data class Left<L : Any>(val value: L) : Either<L, Nothing>(value, null) {
    override fun <T : Any> mapLeft(f: (L) -> T) = Left(f(value))
    override fun <T : Any> mapRight(f: (Nothing) -> T): Nothing = throw IllegalStateException()

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U> = Left(f(value, other.value))
    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (Nothing, T) -> U): Right<U> =
        throw IllegalStateException()
}

data class Right<R : Any>(val value: R) : Either<Nothing, R>(null, value) {
    override fun <T : Any> mapLeft(f: (Nothing) -> T): Nothing = throw IllegalStateException()
    override fun <T : Any> mapRight(f: (R) -> T): Right<T> = Right(f(value))

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (Nothing, T) -> U): Left<U> =
        throw IllegalStateException()

    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U> = Right(f(value, other.value))
}

data class Location(val input: String)
data class ParseError(val stack: List<Pair<Location, String>>);

typealias Parser<A> = (CharSource) -> Either<ParseError, A>

//R - good
interface Parsers {
    fun Location.toError(input: String) = ParseError(listOf(this to input))


    fun <A : Any> run(p: Parser<A>, input: CharSource): Either<ParseError, A>
}

fun String.addSStringAtIndex(str: String, index: Int) =
    StringBuilder(this).apply { insert(index, str) }.toString()

class CharSource constructor(val source: String) {
    var startParsingFrom: Int = 0;
    var parsingNow: Int = 0;
    fun hasNext(): Boolean = parsingNow < source.length

    fun next(): Char {
        parsingNow += 1
        return source[parsingNow - 1];
    }
    fun updateParsingStart() {
        startParsingFrom = parsingNow;
    }
    fun resetParsingNow() {
        parsingNow = startParsingFrom;
    }
    fun generateError(): String {
        return source.addSStringAtIndex("->", minOf(parsingNow, source.length))
    }
}


fun parserPostprocessing(input: CharSource, result: Any) {
    when (result) {
        is Left<*> -> input.resetParsingNow()
        is Right<*> -> input.updateParsingStart()
        else -> throw IllegalAccessException()
    }
}

fun parserPostprocessing(input: CharSource, result: Any, saveInd: Int) {
    when (result) {
        is Left<*> -> {
            input.parsingNow = saveInd; input.startParsingFrom = saveInd
        }
        is Right<*> -> input.updateParsingStart()
        else -> throw IllegalAccessException()
    }
}

interface Combinators {
    fun <T : Any> or_(some: Parser<T>, other: Parser<T>) = { input: CharSource ->
        val saveIndx = input.startParsingFrom;
        val firstValue = some(input)
        parserPostprocessing(input, firstValue, saveIndx)
        when (firstValue) {
            is Left -> {
                val secondValue = other(input)
                parserPostprocessing(input, secondValue, saveIndx)
                secondValue
            }

            else -> firstValue
        }
    }

    fun <T : Any, U : Any> seq_(first: Parser<T>, second: Parser<T>, merge: (T, T) -> U): Parser<U> =
        { input: CharSource ->
            val firstValue = first(input)
            parserPostprocessing(input, firstValue)
            when (firstValue) {
                is Left -> firstValue
                is Right -> {
                    val secondValue = second(input)
                    parserPostprocessing(input, secondValue)
                    when (secondValue) {
                        is Left -> secondValue
                        is Right -> firstValue.mergeRight(secondValue) { a, b -> merge(a, b) }
                    }
                }
            }
        }

    fun <T : Any> test(first: Parser<T>): Parser<T> = { input: CharSource ->
        val saveInd = input.startParsingFrom
        val result = first(input)
        parserPostprocessing(input, Left(""), saveInd)
        result
    }
}


open class DefaultParsers : Parsers, Combinators {
    infix fun Parser<String>.seq(other: Parser<String>) = seq_(this, other) { a: String, b: String ->
        a + b
    }

    infix fun Parser<String>.or(other: Parser<String>) = or_(this, other)

    override fun <A : Any> run(p: Parser<A>, input: CharSource): Either<ParseError, A> = p(input)


    fun expectChar(expected: Char): Parser<String> = { input: CharSource ->
        when (input.hasNext()) {
            true -> {
                when (val c = input.next()) {
                    expected -> Right(c.toString())
                    else -> Left(Location(input.generateError()).toError("Unexpected char in byteparser"))
                }
            }
            else -> Left(Location(input.generateError()).toError("Unexpected Empty Char"))
        }
    }

    fun exceptChars(except: List<Char>): Parser<String> = { input: CharSource ->
        when (input.hasNext()) {
            true -> {
                when (val c = input.next()) {
                    in except -> Left(Location(input.generateError()).toError("Unexpected char"))
                    else -> Right(c.toString())
                }
            }
            else -> Left(Location(input.generateError()).toError("Unexpected Empty Char"))
        }
    }

    fun expectString(expected: String): Parser<String> = { input: CharSource ->
        expected.map { expectChar(it) }.reduce { res, current -> res seq current }(input)
    }

    fun end(): Parser<String> = { input: CharSource ->
        when (input.hasNext()) {
            true -> Left(Location(input.generateError()).toError("Expected end"))
            else -> Right("")
        }
    }

    fun emptyParser(returnValue: String): Parser<String> = { Right(returnValue) }

    fun repeatParserUntilFail(parser: Parser<String>): Parser<String> = { input: CharSource ->
        run {
            val result = mutableListOf<String>();
            while (input.hasNext()) {
                val startIndex = input.startParsingFrom
                val parseResult = parser(input)
                parserPostprocessing(input, parseResult, startIndex)
                when (parseResult) {
                    is Left -> return@run Right(result.fold("") { acc, s -> acc + s })
                    is Right -> result.add(parseResult.value)
                }
            }
            Right(result.fold("") { acc, s -> acc + s })
        }
    }

    fun repeatNotZeroParserUntilFail(parser: Parser<String>): Parser<String> = { input: CharSource ->
        run {
            val result = mutableListOf<String>();
            while (input.hasNext()) {
                val startIndex = input.startParsingFrom
                val parseResult = parser(input)
                parserPostprocessing(input, parseResult, startIndex)
                when (parseResult) {
                    is Left -> {
                        if (result.size == 0) {
                            return@run parseResult
                        } else {
                            return@run Right(result.fold("") { acc, s -> acc + s })
                        }
                    }

                    is Right -> result.add(parseResult.value)
                }
            }
            Right(result.fold("") { acc, s -> acc + s })
        }
    }

}

class ExtendedParsers : DefaultParsers() {
    fun parseParagraph(): Parser<String> = { input: CharSource ->
        val p1 = (expectString("<p>") or emptyParser("<p>")) seq (repeatNotZeroParserUntilFail(
            exceptChars(
                listOf('<', '>')
            )
        ))
        val p2 = expectString("</p>") or test(expectString("<p>")) or test(expectString("<div>")) or test(
            expectString("</div>")
        ) or test(expectString("</body>")) or end()

        when (val result1 = p1(input)) {
            is Right -> {
                when (val result2 = p2(input)) {
                    is Right -> Right(result1.value + "</p>")
                    else -> result2
                }
            }

            else -> result1
        }
    }

    fun parseDiv(): Parser<String> = seq_(
        expectString("<div>") seq repeatParserUntilFail { run(parseParagraph() or parseDiv(), it) },
        (expectString("</div>") or test(expectString("</body>")) or end())
    ) { a, _ -> "$a</div>" }

    fun parseBody(): Parser<String> = seq_((expectString("<body>") or emptyParser("<body>")) seq repeatParserUntilFail {
        run(
            parseParagraph() or parseDiv(),
            it
        )
    }, (expectString("</body>") or end())) { a, _ -> "$a</body>" } seq end()
}