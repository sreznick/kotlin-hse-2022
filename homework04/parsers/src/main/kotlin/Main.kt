import java.io.File
import java.lang.Integer.min

sealed class Either<out L: Any, out R: Any> private constructor(left: Any?, right: Any?) {
    abstract fun <T: Any> mapLeft(f: (L) -> T): Left<T>
    abstract fun <T: Any> mapRight(f: (R) -> T): Right<T>

    abstract fun <T: Any, U: Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U>
    abstract fun <T: Any, U: Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U>
}

data class Left<L: Any>(val value: L) : Either<L, Nothing>(value, null) {
    override fun <T: Any> mapLeft(f: (L) -> T) = Left(f(value))
    override fun <T: Any> mapRight(f: (Nothing) -> T): Nothing = throw IllegalStateException()

    override fun <T: Any, U: Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U> =
        Left(f(value, other.value))
    override fun <T: Any, U: Any> mergeRight(other: Right<T>, f: (Nothing, T) -> U): Right<U> =
        throw IllegalStateException()
}
data class Right<R: Any>(val value: R): Either<Nothing, R>(null, value) {
    override fun <T: Any> mapLeft(f: (Nothing) -> T): Nothing = throw IllegalStateException()
    override fun <T: Any> mapRight(f: (R) -> T) = Right(f(value))

    override fun <T: Any, U: Any> mergeLeft(other: Left<T>, f: (Nothing, T) -> U): Left<U> =
        throw IllegalStateException()
    override fun <T: Any, U: Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U> =
        Right(f(value, other.value))

    fun getValue1() = value
}



data class Location(val input: String, val offset: Int = 0) {
    private val slice by lazy { input.slice(0..offset + 1) }
    val line by lazy { slice.count { it == '\n' } + 1 }
    val column by lazy {
        when (val n = slice.lastIndexOf('\n')) {
            -1 -> offset + 1
            else -> offset - n
        }
    }
}

sealed class Result<A: Any>
data class Success<A: Any>(val a: A, val consumed: Int) : Result<A>()
data class Failure(val get: ParseError) : Result<Nothing>()



data class ParseError(val stack: List<Pair<Location, String>>)

typealias Parser<A> = (String) -> Either<ParseError, A>

internal fun Location.toError(msg: String) = ParseError(listOf(this to msg))

interface Parsers {
    fun <A: Any> run(p: Parser<A>, input: String): Either<ParseError, A>
}


interface CharParsers {
    fun char(c: Char): Parser<Char> = { input: String ->
        if (input.startsWith(c))
            Right(c)
        else Left(Location(input).toError("Expected: $c"))
    }

    fun charRange(from: Char, to: Char): Parser<Char> = { input: String ->
        when {
            input.isEmpty() -> Left(Location(input).toError("Empty string"))
            input[0] in from .. to -> Right(input[0])
            else -> Left(Location(input).toError("Expected: $from - $to"))
        }
    }
}

interface Combinators {
    fun <T: Any> or(some: Parser<T>, other: Parser<T>): Parser<T> = { input: String ->
        val someResult = some(input)
        when (someResult) {
            is Left -> {
                other(input) // TODO: bad error message
            }
            else -> {
                someResult
            }
        }
    }

    fun <T: Any, U: Any> seq(first: Parser<T>, second: Parser<T>, merge: (T, T) -> U): Parser<U> = { input: String ->
        val firstValue = first(input)
        when (firstValue) {
            is Left -> {
                firstValue
            }
            is Right -> {
                when (val secondValue = second(input.drop(1))) {
                    is Left -> {
                        secondValue
                    }
                    is Right -> {
                        firstValue.mergeRight(secondValue) { a, b -> merge(a, b) }
                    }
                }
            }
        }
    }
}

class HtmlParsers:  Parsers, CharParsers, Combinators {
    var correctHTML = ""

    private fun parensP(): Parser<Int> = { input: String ->
        when (val c = input.firstOrNull()) {
            null -> {
                correctHTML += "</p>"
                Right(0)
            }
            '<' -> {
                val substr1 = input.substring(0, min(input.length, 3))
                if (substr1 == "<p>") {
                    correctHTML += "</p>"
                    correctHTML += substr1
                    val p = parensP()
                    val res = p(input.drop(3))
                    res
                }
                val substr2 = input.substring(0, min(input.length, 4))
                if (substr2 == "</p>") {
                    correctHTML += substr2
                    Right(0)
                }
                val substr3 = input.substring(0, min(input.length, 5))
                if (substr3 == "<div>") {
                    correctHTML += "</p>"
                    correctHTML += substr3
                    val p = parensD()
                    val res = p(input.drop(5))
                    res
                }
                val substr4 = input.substring(0, min(input.length, 6))
                if (substr4 == "</div>") {
                    correctHTML += "</p>"
                    Right(0)
                }
                val substr5 = input.substring(0, min(input.length, 6))
                if (substr5 == "<body>") {
                    Left(Location(input).toError("Wrong element"))
                }
                val substr6 = input.substring(0, min(input.length, 7))
                if (substr6 == "</body>") {
                    correctHTML += "</p>"
                    Right(0)
                }
                Left(Location(input).toError("Wrong element"))
            }
            else ->  {
                correctHTML += c
                val p = parensP()
                val res = p(input.drop(1))
                res
            }
        }

    }

    private fun parensD(): Parser<Int> = { input: String ->
        when (val c = input.firstOrNull()) {
            null -> {
                correctHTML += "</div>"
                Right(0)
            }
            '<' -> {
                val substr1 = input.substring(0, min(input.length, 3))
                if (substr1 == "<p>") {
                    correctHTML += substr1
                    val p = parensP()
                    val res = p(input.drop(3))
                    correctHTML += "</div>"
                    res
                }
                val substr3 = input.substring(0, min(input.length, 5))
                if (substr3 == "<div>") {
                    correctHTML += substr3
                    val p = parensD()
                    val res = p(input.drop(5))
                    correctHTML += "</div>"
                    res
                }
                val substr4 = input.substring(0, min(input.length, 6))
                if (substr4 == "</div>") {
                    correctHTML += substr4
                    Right(0)
                }
                val substr6 = input.substring(0, min(input.length, 7))
                if (substr6 == "</body>") {
                    correctHTML += "</div>"
                    Right(0)
                }
                Left(Location(input).toError("Wrong element"))
            }
            else ->  {
                correctHTML += "<p>"
                correctHTML += c
                val p = parensP()
                val res = p(input.drop(1))
                correctHTML += "</div>"
                res
            }
        }

    }

    fun parens(): Parser<Int> = { input: String ->
        when (val c = input.firstOrNull()) {
            null -> {
                correctHTML += "</body>"
                Right(0)
            }
            '<' -> {
                var res : Either<ParseError, Int> = Left(Location(input).toError("Wrong element"))
                val substr1 = input.substring(0, min(input.length, 3))
                if (substr1 == "<p>") {
                    val p = parensP()
                    correctHTML += substr1
                    res = p(input.drop(3))
                    correctHTML += "</body>"
                }
                val substr3 = input.substring(0, min(input.length, 5))
                if (substr3 == "<div>") {
                    correctHTML += substr3
                    val p = parensD()
                    res = p(input.drop(5))
                    correctHTML += "</body>"
                }
                val substr5 = input.substring(0, min(input.length, 6))
                if (substr5 == "<body>") {
                    correctHTML += substr5
                    val p = parens()
                    res = p(input.drop(6))
                }
                val substr6 = input.substring(0, min(input.length, 7))
                if (substr6 == "</body>") {
                    correctHTML += substr6
                    Right(0)
                }
                res
            }
            else ->  {
                correctHTML += "<p>"
                val p = parensP()
                val res = p(input)
                correctHTML +=  "</body>"
                res
            }
        }

    }


    override fun <A: Any> run(p: Parser<A>, input: String): Either<ParseError, A> = p(input)
}

fun mytests() {
    val pp = HtmlParsers()
    val a = "<body></body>"
    val b = "<body>"
    val c = "<body>A"
    val d = "<body>A</body>"
    val e = "<body>A<div>B"
    val f = "<body>A<div>B<div>C"
    val g = "<body><p>A<div>B<div>C"
    val h = "<body><div><p>A<div>B<div>C"
    val i = "<body><div><p>A<div>B<div>C</div>"
    val p = pp.parens()
    pp.run(p, a)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, b)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, c)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, d)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, e)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, f)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, g)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, h)
    println(pp.correctHTML)
    pp.correctHTML = ""
    pp.run(p, i)
    println(pp.correctHTML)
//<body></body>
//<body></body>
//<body><p>A</p></body>
//<body><p>A</p></body>
//<body><p>A</p><div><p>B</p></div></body>
//<body><p>A</p><div><p>B</p><div><p>C</p></div></div></body>
//<body><p>A</p><div><p>B</p><div><p>C</p></div></div></body>
//<body><div><p>A</p><div><p>B</p><div><p>C</p></div></div></div></body>
//<body><div><p>A</p><div><p>B</p><div><p>C</p></div></div></div></body>
}

fun main(args: Array<String>) {
    val fileName = args[0]
    var text = ""
    File(fileName).forEachLine{text += it}
    val parsers = HtmlParsers()
    val p = parsers.parens()
    parsers.run(p, text)
    File("src/output/correctHmll.txt" ).writeText(parsers.correctHTML)
}