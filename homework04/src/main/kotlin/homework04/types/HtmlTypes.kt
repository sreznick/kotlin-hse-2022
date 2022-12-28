package homework04.types

sealed class InnerTag() {
    abstract fun toStringBuilder(s: StringBuilder)
}

data class P(val text: String) : InnerTag() {
    override fun toStringBuilder(s: StringBuilder) {
        s.append("<p>").append(text).append("</p>")
    }
}

data class Div(val inner: List<InnerTag>) : InnerTag() {
    override fun toStringBuilder(s: StringBuilder) {
        s.append("<div>").apply { inner.forEach { it.toStringBuilder(s) } }.append("</div>")
    }
}

data class Body(val inner: List<InnerTag>) {
    fun toStringBuilder(s: StringBuilder) {
        s.append("<body>").apply { inner.forEach { it.toStringBuilder(s) } }.append("</body>")
    }
}

data class Html(val body: Body) {
    override fun toString(): String {
        val s = StringBuilder()
        body.toStringBuilder(s)
        return s.toString()
    }
}