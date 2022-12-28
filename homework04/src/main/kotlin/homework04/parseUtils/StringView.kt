package homework04.parseUtils

class StringView(private val value: String, private val left: Int = 0, private val length: Int = value.length) :
    View<String, Char> {

    override fun convert(): String {
        return value.substring(left, left + length)
    }

    override fun get(index: Int): Char {
        return value[left + index]
    }

    override fun substring(index: Int, count: Int): StringView =
        if (count == -1) StringView(value, left + index, length - index)
        else StringView(value, left + index, count.coerceAtMost(length))


    override fun startsWith(elem: Char): Boolean {
        return length > 0 && value[left] == elem
    }

    fun startsWith(elem: String): Boolean {
        return elem.length <= size() && (elem.indices).fold(true) { acc, ind -> acc and (get(ind) == elem[ind]) }
    }

    fun slice(indices: IntRange): String {
        return value.slice(left + indices.first..left + indices.last)
    }

    override fun isEmpty(): Boolean {
        return length == 0
    }

    override fun size(): Int {
        return length
    }

}