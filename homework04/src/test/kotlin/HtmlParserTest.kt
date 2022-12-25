import html.HtmlParser
import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlParserTest {

    private val parsers = HtmlParser()

    @Test
    fun testP() {
        val parser = parsers.parseHtml()
        val tests = mapOf(
            "<body><p>text</p></body>" to "<body><p>text</p></body>",
            "<body><p>text1<p>text2</p></body>" to "<body><p>text1</p><p>text2</p></body>",
            "<body><p>text1<div><p>text2</p></div></body>" to "<body><p>text1</p><div><p>text2</p></div></body>",
        )
        parsers.apply {
            for ((test, ans) in tests) {
                val res = (run(parser, test) as Right).value.a.toString().replace("\n", "").replace("\t", "")
                assertEquals(ans, res)

            }
        }
    }

    @Test
    fun testDiv() {
        val parser = parsers.parseHtml()
        val tests = mapOf(
            "<body><div></body>" to "<body><div></div></body>",
            "<body><div><div></body>" to "<body><div><div></div></div></body>"
        )
        parsers.apply {
            for ((test, ans) in tests) {
                val res = (run(parser, test) as Right).value.a.toString().replace("\n", "").replace("\t", "")
                assertEquals(ans, res)

            }
        }
    }

    @Test
    fun testBody() {
        val parser = parsers.parseHtml()
        val tests = mapOf(
            "<body><div><p>" to "<body><div><p></p></div></body>",
            "<body><div><p>text" to "<body><div><p>text</p></div></body>",
            "<div></div>" to "<body><div></div></body>",
            "text" to "<body><p>text</p></body>"
        )
        parsers.apply {
            for ((test, ans) in tests) {
                val res = (run(parser, test) as Right).value.a.toString().replace("\n", "").replace("\t", "")
                assertEquals(ans, res)

            }
        }
    }
}