package homework04.parseUtils

class ByteArrayView(private val value: ByteArray, private val left: Int = 0, private val length: Int = value.size) :
    View<ByteArray, Byte> {

    override fun convert(): ByteArray {
        return value.sliceArray(left until left + length)
    }

    override fun get(index: Int): Byte {
        return value[left + index]
    }

    override fun substring(index: Int, count: Int): ByteArrayView =
        if (count == -1) ByteArrayView(value, left + index, length - index)
        else ByteArrayView(value, left + index, length.coerceAtMost(count))


    override fun startsWith(elem: Byte): Boolean {
        return length > 0 && value[left] == elem
    }

    override fun isEmpty(): Boolean {
        return length == 0
    }

    override fun size(): Int {
        return length
    }
}
