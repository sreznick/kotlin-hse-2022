import java.io.File

class HTMLApp(private val parser: HTMLParser = HTMLParser()) {
    fun parseHTML(fileIn: String, fileOut: String) {
        when (val parsedHTML: Result<Body> = parser.run(parser.body(), File(fileIn).readText())) {
            is Success -> File(fileOut).writeText(parsedHTML.value.toString())
            is Failure -> {
                println("Something went wrong:")
                println(parsedHTML.get.stack)
            }
        }
    }
}

fun main(args: Array<String>) {
    val app = HTMLApp()
    app.parseHTML(args[0], args[1])
}
