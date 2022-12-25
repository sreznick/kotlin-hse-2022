package html

open class HtmlElement {

    protected fun addChildren(children: List<HtmlElement>) = buildString {
        for (child in children) {
            append("\n")
            append(child)
        }
    }.replace("\n", "\n\t")

    class Body(private val children: List<HtmlElement>): HtmlElement() {
        override fun toString(): String = buildString {
            append("<body>")
            append(addChildren(children))
            append("\n")
            append("</body>")
        }
    }

    class Div(private val children: List<HtmlElement>): HtmlElement() {
        override fun toString(): String = buildString {
            append("<div>")
            val str = buildString {
                for (child in children) {
                    append("\n")
                    append(child)
                }
            }.replace("\n", "\n\t")
            append(addChildren(children))
            append("\n")
            append("</div>")
        }
    }

    class P(private val text: String): HtmlElement() {
        override fun toString(): String = buildString {
            append("<p>\n\t")
            append(text)
            append("\n")
            append("</p>")
        }
    }

}
