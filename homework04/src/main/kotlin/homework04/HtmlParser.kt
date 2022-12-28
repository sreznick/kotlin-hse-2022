package homework04

import java.io.File
import homework04.parseUtils.*
import homework04.types.*

class HtmlParser : Combinators(), CharParsers, StringParsers {

    private val textParser = map(repeatAtLeastOnceUntilFailure(charNotSet(setOf('<', '>')))) { it.joinToString("") }

    private fun pTagParser(): Parser<StringView, InnerTag> =
        seq(string("<p>"), (textParser or nothing()), (string("</p>") or nothing())) { list -> P(list[1] as String) }

    private fun divTagParser(): Parser<StringView, InnerTag> = seq(
        string("<div>"),
        repeatUntilFailure { run(innerTagParser(), it) },
        (string("</div>") or nothing())
    ) { list -> Div(list[1] as List<InnerTag>) }

    private fun innerTagParser(): Parser<StringView, InnerTag> =
        map(textParser) { P(it) as InnerTag } or pTagParser() or divTagParser()

    private fun bodyParser(): Parser<StringView, Body> =
        seq(string("<body>"), repeatUntilFailure(innerTagParser())) { _, b -> Body(b) }

    private fun htmlParser(): Parser<StringView, Html> =
        map(bodyParser() or map(repeatUntilFailure(innerTagParser())) { Body(it) }) { Html(it) }

    fun parse(input: String): Result<StringView, Html> = run(htmlParser(), StringView(input))
}

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Expected two files: for input and output")
        return
    }
    val inputFile = File(args[0])
    val outputFile = File(args[1])
    if (!inputFile.isFile) {
        println("Given input file doesn't exist")
        return
    }
    val parser = HtmlParser()
    when (val parsed = parser.parse(inputFile.readText())) {
        is Failure -> println("Parse error: ${parsed.get}")
        is Success -> {
            val result = parsed.a.toString()
            println(result)
            outputFile.writeText(result)
        }
    }
}
