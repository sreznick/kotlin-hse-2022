package html

import Either
import Failure
import Location
import Success
import parsers.*

class HtmlParser : Parsers, Combinators, CharParsers, StringParsers {

    private fun <T: Any> skipWs(neutral: T) = ignore(skip(chars("\t\n "), ""), neutral)

    private fun parsePText() = star(charExcept("<>"), "") { a, b -> "$a$b" }

    private fun parsePTextNotEmpty() = plus(charExcept("<>")) { a, b -> "$a$b" }

    private fun parseP(): Parser<HtmlElement.P, String> =
        map(
            or(seqList(
                skipWs(listOf()),
                ignore(string("<p>"), listOf()),
                map(parsePText()) { listOf(HtmlElement.P(it)) },
                ignore(maybe(string("</p>"), ""), listOf()),
                skipWs(listOf())
            ) { a, b -> a + b },
                seqList(
                    skipWs(listOf()),
                    ignore(maybe(string("<p>"), ""), listOf()),
                    map(parsePTextNotEmpty()) { listOf(HtmlElement.P(it)) },
                    ignore(maybe(string("</p>"), ""), listOf()),
                    skipWs(listOf())
                ) { a, b -> a + b }

            )) { it[0] }

    private fun parseDiv(): Parser<HtmlElement.Div, String> = { input ->
        map(seqList(
            skipWs(listOf()),
            ignore(string("<div>"), listOf()),
            star(
                or(
                    map(parseP()) { listOf(it) },
                    map(parseDiv()) { listOf(it) }
                ), listOf()
            ) { a, b -> a + b },
            skipWs(listOf()),
            ignore(maybe(string("</div>"), ""), listOf()),
            skipWs(listOf())
        ) { a, b -> a + b }) { HtmlElement.Div(it) }(input)
    }

    private fun parseBody(): Parser<HtmlElement.Body, String> =
        map(seqList(
            ignore(maybe(string("<body>"), ""), listOf()),
            star(
                or(
                    map(parseDiv()) { listOf(it) },
                    map(parseP()) { listOf(it) },
                ), listOf()
            ) { a, b -> a + b },
            ignore(maybe(string("</body>"), ""), listOf())
        ) { a, b -> a + b }) { HtmlElement.Body(it) }


    fun parseHtml() = parseBody()

    override fun <A : Any, B : Any> run(p: Parser<A, B>, input: B): Either<Failure<B>, Success<out A>> {
        val location = Location(input = input, offset = 0)
        return p(location)
    }
}
