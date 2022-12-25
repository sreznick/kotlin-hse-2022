package bytecode

import Either
import Failure
import Left
import Location
import Right
import Success
import parsers.ByteParsers
import parsers.Combinators
import parsers.Parser
import parsers.Parsers
import java.io.Serializable
import kotlin.math.pow


class ByteCodeParser : Parsers, ByteParsers, Combinators {

    private val data: JavaByteCode = JavaByteCode()

    private fun <T : Any> bytesSeq(merge: (T, T) -> T, convert: (Byte) -> T, vararg bytes: Byte): Parser<T, ByteArray> {
        val arr: List<Parser<T, ByteArray>> = bytes.map { byte(it, convert) }

        return seqList(arr) { a, b -> merge(a, b) }
    }

    private fun parseTwoBytesAsInt(
        byte1Range: Pair<UByte, UByte> = 0.toUByte() to UByte.MAX_VALUE,
        byte2Range: Pair<UByte, UByte> = 0.toUByte() to UByte.MAX_VALUE,
        merge: (UByte, UByte) -> Int = { a, b -> (a.toInt() shl 8) and 0xFF00 or b.toInt() },
        f: ((Int) -> Unit)? = null
    ): Parser<Int, ByteArray> = seq(
        unsignedByteRange(byte1Range.first, byte1Range.second) { it.toUByte() },
        unsignedByteRange(byte2Range.first, byte2Range.second) { it.toUByte() },
        merge,
        f
    )

    private fun parseFourBytesAsInt() =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b ->
            (a shl 8) and 0xFF00 or b
        })

    private fun parseMagic(): Parser<Boolean, ByteArray> =
        bytesSeq({ a, b -> a && b }, { true }, -54, -2, -70, -66)

    private fun parseMinorVersion(): Parser<Int, ByteArray> = parseTwoBytesAsInt { data.minorVersion = it }

    private fun parseMajorVersion(): Parser<Int, ByteArray> =
        parseTwoBytesAsInt(byte2Range = 0.toUByte() to 63u) { data.minorVersion = it }


    private fun parseClass(constantPool: ConstantPool, index: Int): Parser<Int, ByteArray> =
        parseTwoBytesAsInt { constantPool.constantClassIndex[index] = it }

    private fun parseFieldRef(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> Pair(a, b) }) { constantPool.fieldRef = it }

    private fun parseMethodRef(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> Pair(a, b) }) { constantPool.methodRef = it }

    private fun parseInterfaceMethodRef(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> Pair(a, b) }) { constantPool.interfaceMethodRef = it }

    private fun parseString(constantPool: ConstantPool, index: Int): Parser<Int, ByteArray> =
        parseTwoBytesAsInt { constantPool.stringIndex.add(it) }

    private fun parseInteger(constantPool: ConstantPool, index: Int): Parser<Int, ByteArray> =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> (a shl 8) and 0xFF00 or b }) {
            constantPool.integer.add(it)
        }


    private fun parseFloat(constantPool: ConstantPool, index: Int): Parser<Float, ByteArray> {
        fun convertToFloat(a: Int, b: Int): Float {
            val bits = (a shl 8) and 0xFF00 or b
            if (bits == 0x7f800000) return Float.POSITIVE_INFINITY
            if (bits == -8388608) return Float.NEGATIVE_INFINITY
            return if (bits in 0x7f800001..0x7fffffff || bits in -8388607..-1) Float.NaN
            else {
                val s = if (bits shr 31 == 0) 1 else -1
                val e = bits shr 23 and 0xff
                val m = if (e == 0) bits and 0x7fffff shl 1 else bits and 0x7fffff or 0x800000
                s * m * 2f.pow(e - 150)
            }
        }
        return seq(
            parseTwoBytesAsInt(),
            parseTwoBytesAsInt(),
            { a: Int, b: Int -> convertToFloat(a, b) }) { constantPool.float.add(it) }
    }

    private fun parseLong(constantPool: ConstantPool, index: Int): Parser<Long, ByteArray> =
        seq(
            seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> (a shl 8) and 0xFF00 or b }),
            seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> (a shl 8) and 0xFF00 or b }),
            { a: Int, b: Int -> (a.toLong() shl 32) and 0xFFFF0000 or b.toLong() }) { constantPool.long.add(it) }


    private fun parseDouble(constantPool: ConstantPool, index: Int): Parser<Double, ByteArray> {
        fun convertToDouble(a: Int, b: Int): Double {
            val bits: Long = (a.toLong() shl 32) + b
            if (bits == 0x7ff0000000000000L) return Double.POSITIVE_INFINITY
            if (bits == -4503599627370496) return Double.NEGATIVE_INFINITY
            return if (bits in 0x7ff0000000000001L..0x7ff0000000000001L || bits in -4503599627370495..-1) Double.NaN
            else {
                val s = if (bits shr 63 == 0L) 1 else -1
                val e = (bits shr 52 and 0x7ffL).toInt()
                val m = if (e == 0) bits and 0xfffffffffffffL shl 1 else bits and 0xfffffffffffffL or 0x10000000000000L
                s * m * 2.0.pow(e - 1075)
            }
        }
        return seq(
            seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> (a shl 8) and 0xFF00 or b }),
            seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> (a shl 8) and 0xFF00 or b }),
            { a, b -> convertToDouble(a, b) }) { constantPool.double.add(it) }
    }

    private fun parseNameAndType(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(parseTwoBytesAsInt(), parseTwoBytesAsInt(), { a, b -> Pair(a, b) }) { constantPool.nameAndType = it }

    private fun parseUtf8(constantPool: ConstantPool, index: Int): Parser<String, ByteArray> =
        { input: Location<ByteArray> ->
            fun parseUtf8String(amount: Int): Parser<String, ByteArray> {
                fun parseUtf8Char(): Parser<String, ByteArray> = { input: Location<ByteArray> ->
                    val oneByteChar = byteRange(0, 127) { Char(it.toInt()).toString() }(input)
                    when (oneByteChar) {
                        is Right -> oneByteChar
                        is Left -> {
                            val twoBytesChar = seq(
                                unsignedByteRange(192u, 223u) { it.toUByte() },
                                unsignedByteRange(128u, 191u) { it.toUByte() },
                                { x, y -> Char(((x.toInt() and 0x1f) shl 6) + (y.toInt() and 0x3f)).toString() })(input)
                            when (twoBytesChar) {
                                is Right -> twoBytesChar
                                is Left -> {
                                    val threeBytesChar = seq(
                                        seq(
                                            unsignedByteRange(224u, 239u) { it.toUByte() },
                                            unsignedByteRange(128u, 191u) { it.toUByte() },
                                            { x, y -> ((x.toInt() and 0xf) shl 12) + ((y.toInt() and 0x3f) shl 6) }
                                        ),
                                        unsignedByteRange(128u, 191u) { it.toUByte() },
                                        { a, z -> Char(a + (z.toInt() and 0x3f)).toString() }
                                    )(input)
                                    when (threeBytesChar) {
                                        is Right -> threeBytesChar
                                        is Left -> {
                                            val fourBytesChar = map(seqList(
                                                ignore(byte(-19) { it.toInt() }, listOf()),
                                                unsignedByteRange(160u, 175u) { listOf(it.toInt()) },
                                                unsignedByteRange(128u, 191u) { listOf(it.toInt()) },
                                                ignore(byte(-19) { it.toInt() }, listOf()),
                                                unsignedByteRange(176u, 191u) { listOf(it.toInt()) },
                                                unsignedByteRange(128u, 191u) { listOf(it.toInt()) }
                                            ) { a, b -> a + b }) { arr ->
                                                Char(
                                                    0x10000 + (arr[0] and 0x0f shl 16) + (arr[1] and 0x3f shl 10) +
                                                            (arr[2] and 0x0f shl 6) + (arr[3] and 0x3f)
                                                ).toString()
                                            }(input)
                                            fourBytesChar
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return starUpTo(parseUtf8Char(), amount, "") { a, b -> "$a$b" }
            }
            readLengthThenData(
                parseTwoBytesAsInt(),
                { parseUtf8String(it) },
            ) { constantPool.utf_8[index] = it }(input)
        }

    private fun parseMethodHandle(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(
            unsignedByteRange(0u, UByte.MAX_VALUE) { it.toUByte().toInt() },
            parseTwoBytesAsInt(),
            { a, b -> Pair(a, b) }
        ) { constantPool.methodHandle = it }

    private fun parseMethodType(constantPool: ConstantPool, index: Int) =
        parseTwoBytesAsInt { constantPool.methodType = it }

    private fun parseDynamic(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(
            parseTwoBytesAsInt(),
            parseTwoBytesAsInt(),
            { a, b -> Pair(a, b) }
        ) { constantPool.dynamic = it }

    private fun parseInvokeDynamic(constantPool: ConstantPool, index: Int): Parser<Pair<Int, Int>, ByteArray> =
        seq(
            parseTwoBytesAsInt(),
            parseTwoBytesAsInt(),
            { a, b -> Pair(a, b) }
        ) { constantPool.invokeDynamic = it }

    private fun parseModule(constantPool: ConstantPool, index: Int): Parser<Int, ByteArray> =
        parseTwoBytesAsInt { constantPool.module = it }

    private fun parsePackage(constantPool: ConstantPool, index: Int): Parser<Int, ByteArray> =
        parseTwoBytesAsInt { constantPool.constantPackage = it }


    private fun parseConstantPoolTable(amount: Int): Parser<ConstantPool, ByteArray> =
        { input: Location<ByteArray> ->
            val poolByTag: Map<Int, (ConstantPool, Int) -> Parser<out Serializable, ByteArray>> = mapOf(
                1 to { it, index -> parseUtf8(it, index) },
                3 to { it, index -> parseInteger(it, index) },
                4 to { it, index -> parseFloat(it, index) },
                5 to { it, index -> parseLong(it, index) },
                6 to { it, index -> parseDouble(it, index) },
                7 to { it, index -> parseClass(it, index) },
                8 to { it, index -> parseString(it, index) },
                9 to { it, index -> parseFieldRef(it, index) },
                10 to { it, index -> parseMethodRef(it, index) },
                11 to { it, index -> parseInterfaceMethodRef(it, index) },
                12 to { it, index -> parseNameAndType(it, index) },
                15 to { it, index -> parseMethodHandle(it, index) },
                16 to { it, index -> parseMethodType(it, index) },
                17 to { it, index -> parseDynamic(it, index) },
                18 to { it, index -> parseInvokeDynamic(it, index) },
                19 to { it, index -> parseModule(it, index) },
                20 to { it, index -> parsePackage(it, index) }
            )
            val constantPool = ConstantPool()

            fun rec(p: Parser<Int, ByteArray>, amountLast: Int, consumed: Int): Parser<Serializable, ByteArray> =
                { input: Location<ByteArray> ->
                    if (amountLast <= 0) Right(Success("", consumed))
                    else {
                        val firstValue = p(input)
                        when (firstValue) {
                            is Left -> firstValue
                            is Right -> {
                                val second = poolByTag[firstValue.value.a]!!
                                val secondValue = second(constantPool, amount - amountLast)(
                                    Location(
                                        input.input,
                                        input.offset + firstValue.value.consumed
                                    )
                                )
                                when (secondValue) {
                                    is Left -> secondValue
                                    is Right -> {
                                        if (firstValue.value.a == 5 || firstValue.value.a == 6) {
                                            rec(
                                                p,
                                                amountLast - 2,
                                                consumed + firstValue.value.consumed + secondValue.value.consumed
                                            )(
                                                Location(
                                                    input.input,
                                                    input.offset + firstValue.value.consumed + secondValue.value.consumed
                                                )
                                            )
                                        } else {
                                            rec(
                                                p,
                                                amountLast - 1,
                                                consumed + firstValue.value.consumed + secondValue.value.consumed
                                            )(
                                                Location(
                                                    input.input,
                                                    input.offset + firstValue.value.consumed + secondValue.value.consumed
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            val res = rec(byteRange(1, 20) { it.toInt() }, amount - 1, 0)(input)
            when (res) {
                is Left -> res
                is Right -> {
                    Right(Success(constantPool, res.value.consumed))
                }
            }
        }

    private fun parseConstantPool(): Parser<ConstantPool, ByteArray> =
        readLengthThenData(
            parseTwoBytesAsInt(),
            { parseConstantPoolTable(it) }
        ) { data.constantPool = it }

    private fun parseAccessFlags(): Parser<Int, ByteArray> = parseTwoBytesAsInt { data.accessFlag = it }

    private fun parseThisClass(): Parser<Int, ByteArray> = parseTwoBytesAsInt { data.thisClass = it }

    private fun parseSuperClass(): Parser<Int, ByteArray> = parseTwoBytesAsInt { data.superClass = it }

    private fun parseTwoBytesAsIntToList() =
        seq(unsignedByteRange(0u, UByte.MAX_VALUE) { x -> listOf(x.toUByte().toInt()) },
            unsignedByteRange(0u, UByte.MAX_VALUE) { x -> listOf(x.toUByte().toInt()) },
            { a, b -> listOf((a[0] shl 8) and 0xFF00 or b[0]) })

    private fun parseInterfaces(): Parser<List<Int>, ByteArray> =
        readLengthThenData(
            parseTwoBytesAsInt(),
            {
                starUpTo(
                    parseTwoBytesAsIntToList(),
                    it * 2,
                    listOf()
                ) { a: List<Int>, b: List<Int> -> a + b }
            }
        ) { data.interfaces = it }

    private fun skipAttributes(count: Int): Parser<List<Int>, ByteArray> {
        fun skipSingleAttribute(): Parser<List<Int>, ByteArray> =
            seq(
                parseTwoBytesAsInt(),
                readLengthThenData(
                    parseFourBytesAsInt(),
                    { skipN(it, listOf<Int>()) { arr -> arr.size } }
                ),
                { a, _ -> listOf(a) }
            )
        if (count == 0) return emptyParser(listOf())
        return seqList((0 until count).map { skipSingleAttribute() }) { a, b -> a }
    }

    private fun <T : Any> parseAttributesAware(
        buildClassFromList: (List<Int>, List<Int>) -> List<T>,
        f: (List<T>) -> Unit
    ): Parser<List<T>, ByteArray> {
        fun parseFieldsAfterCount(count: Int): Parser<List<T>, ByteArray> {
            fun parseSingleField(): Parser<List<T>, ByteArray> {
                return seq(
                    seqList(
                        parseTwoBytesAsIntToList(),
                        parseTwoBytesAsIntToList(),
                        parseTwoBytesAsIntToList(),
                    ) { a, b -> a + b },
                    readLengthThenData(
                        parseTwoBytesAsInt(),
                        { skipAttributes(it) }
                    ),
                    { a: List<Int>, b: List<Int> -> buildClassFromList(a, b) }
                )
            }
            if (count == 0) return emptyParser(listOf())
            return seqList((0 until count).map { parseSingleField() }) { a, b -> a + b }
        }

        return readLengthThenData(
            parseTwoBytesAsInt(),
            { parseFieldsAfterCount(it) }
        ) { f(it) }
    }

    private fun parseFields() = parseAttributesAware(
        { a, b -> listOf(Info(a[0], a[1], a[2], b)) },
        { data.fieldInfo = it }
    )

    private fun parseMethods() = parseAttributesAware(
        { a, b -> listOf(Info(a[0], a[1], a[2], b)) },
        { data.methodInfo = it }
    )

    private fun parseAttributes() = readLengthThenData(
        parseTwoBytesAsInt(),
        { skipAttributes(it) }
    ) { data.classAttributesInfo = it }

    fun parseByteCode() = map(seqList(
        parseMagic(),
        parseMinorVersion(),
        parseMajorVersion(),
        parseConstantPool(),
        parseAccessFlags(),
        parseThisClass(),
        parseSuperClass(),
        parseInterfaces(),
        parseFields(),
        parseMethods(),
        parseAttributes()
    ) { a, b -> "" }) { data }

    override fun <A : Any, B : Any> run(p: Parser<A, B>, input: B): Either<Failure<B>, Success<out A>> {
        val location = Location(input = input, offset = 0)
        return p(location)
    }
}