package homework04.parseUtils

class ByteLocation(override val input: ByteArrayView, override val offset: Int = 0) : Location<ByteArrayView>

open class ByteParsers : Combinators(), Parsers {

    protected fun anyByte(): Parser<ByteArrayView, Byte> = { input: ByteArrayView ->
        if (input.isEmpty())
            Failure(ByteLocation(input).toError("Expected: byte"))
        else {
            Success((input.get(0)), 1)
        }
    }

    protected fun nByte(n: Int): Parser<ByteArrayView, ByteArray> = map(repeatParser(anyByte(), n)) { it.toByteArray() }

    @OptIn(ExperimentalUnsignedTypes::class)
    protected fun bytesToInt(bytes: ByteArray): Int {
        if (bytes.isEmpty()) return 0
        var result = 0
        var shift = 0
        for (byte in bytes.toUByteArray().reversed()) {
            result = result or (byte.toInt() shl shift)
            shift += 8
        }
        return result
    }

    protected fun bytesToString(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)

    private fun byteMerge(a: Byte, b: Byte) = ByteArray(0).plus(a).plus(b)
    private fun byteArrayMerge(a: ByteArray, b: ByteArray) = a.plus(b)

    private val anyByteParser = anyByte()
    protected val twoByteParser = seq(anyByteParser, anyByteParser, ::byteMerge)
    protected val fourByteParser = seq(twoByteParser, twoByteParser, ::byteArrayMerge)
}