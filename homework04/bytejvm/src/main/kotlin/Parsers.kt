import java.lang.IllegalArgumentException

val accessFlagsMap = mapOf(
    0x0001 to "ACC_PUBLIC",
    0x0010 to "ACC_FINAL",
    0x0020 to "ACC_SUPER",
    0x0200 to "ACC_INTERFACE",
    0x0400 to "ACC_ABSTRACT",
    0x1000 to "ACC_SYNTHETIC",
    0x2000 to "ACC_ANNOTATION",
    0x4000 to "ACC_ENUM",
    0x8000 to "ACC_MODULE"
)
val accessFlagsFields = mapOf(
    0x0001 to "ACC_PUBLIC",
    0x0002 to "ACC_PRIVATE",
    0x0004 to "ACC_PROTECTED",
    0x0008 to "ACC_STATIC",
    0x0010 to "ACC_FINAL",
    0x0040 to "ACC_VOLATILE",
    0x0080 to "ACC_TRANSIENT",
    0x1000 to "ACC_SYNTHETIC",
    0x4000 to "ACC_ENUM"
)
val accessFlagsMethods = mapOf(
    0x0001 to "ACC_PUBLIC",
    0x0002 to "ACC_PRIVATE",
    0x0004 to "ACC_PROTECTED",
    0x0008 to "ACC_STATIC",
    0x0010 to "ACC_FINAL",
    0x0020 to "ACC_SYNCHRONIZED",
    0x0040 to "ACC_BRIDGE",
    0x0080 to "ACC_VARARGS",
    0x0100 to "ACC_NATIVE",
    0x0400 to "ACC_ABSTRACT",
    0x0800 to "ACC_STRICT",
    0x1000 to "ACC_SYNTHETIC"
)


val descriptorMap = mapOf(
    "B" to "byte",
    "C" to "char",
    "D" to "double",
    "F" to "float",
    "I" to "int",
    "J" to "long",
    "S" to "short",
    "Z" to "boolean",
    "s" to "String",
    "e" to "Enum",
    "c" to "Class",
    "@" to "Annotation interface",
    "[" to "Array type"
)


@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toInt(): Int {
    return this.map { it.toString(16).padStart(2, '0') }.reduce { acc, current -> acc + current }.toInt(16)
}


class Field(fieldRaw: List<Int>, constantPool: List<Constant>) {
    val accessFlags: List<String> = if (fieldRaw.size >= 4) {
        val flagsMask = fieldRaw[0]
        accessFlagsFields.entries.filter { entry -> entry.key and flagsMask != 0 }.map { entry -> entry.value }
            .toList()
    } else {
        throw IllegalArgumentException()
    }

    val name: String = if (fieldRaw.size >= 4) {
        (constantPool[fieldRaw[1] - 1] as UTF8Constant).value
    } else {
        throw IllegalArgumentException()
    }

    val descriptor: String = if (fieldRaw.size >= 4) {
        val valueRaw = (constantPool[fieldRaw[2] - 1] as UTF8Constant).value
        descriptorMap.getOrDefault(valueRaw, valueRaw)
    } else {
        throw IllegalArgumentException()
    }

    val attributes: List<String> = if (fieldRaw.size >= 4) {
        fieldRaw.slice(4 until fieldRaw.size).map { constantPool[it - 1] }.filterIsInstance<UTF8Constant>()
            .map { it.value }
    } else {
        throw IllegalArgumentException()
    }
}

class Method(fieldRaw: List<Int>, constantPool: List<Constant>) {
    val accessFlags: List<String> = if (fieldRaw.size >= 4) {
        val flagsMask = fieldRaw[0]
        accessFlagsMethods.entries.filter { entry -> entry.key and flagsMask != 0 }.map { entry -> entry.value }
            .toList()
    } else {
        throw IllegalArgumentException()
    }

    val name: String = if (fieldRaw.size >= 4) {
        (constantPool[fieldRaw[1] - 1] as UTF8Constant).value
    } else {
        throw IllegalArgumentException()
    }

    val descriptor: String = if (fieldRaw.size >= 4) {
        val valueRaw = (constantPool[fieldRaw[2] - 1] as UTF8Constant).value
        descriptorMap.getOrDefault(valueRaw, valueRaw)
    } else {
        throw IllegalArgumentException()
    }

    val attributes: List<String> = if (fieldRaw.size >= 4) {
        fieldRaw.slice(4 until fieldRaw.size).map { constantPool[it - 1] }.filterIsInstance<UTF8Constant>()
            .map { it.value }
    } else {
        throw IllegalArgumentException()
    }
}


class Result @OptIn(ExperimentalUnsignedTypes::class) constructor(
    val magicNumber: Boolean? = null,
    val minorVersion: Int? = null,
    val majorVersion: Int? = null,
    val accessFlagBin: UByteArray? = null,
    val constantPool: List<Constant>? = null,
    val mainClassNameRef: Int? = null,
    val superClassNameRef: Int? = null,
    val interfacesRefs: List<Int>? = null,
    val fieldsRaw: List<List<Int>>? = null,
    val methodsRaw: List<List<Int>>? = null,
    val attributesRaw: List<Int>? = null
) {
    override fun toString(): String {
        return "magic = $magicNumber, minor = $minorVersion, major = $majorVersion, access_flags = $accessFlags, main_class_name = $mainClassName, super_class_name = $superClassName, interfaces = $interfacesHuman"
    }

    private fun getMainOrSuperName(ref: Int?): String? {
        if (ref != null) {
            val constant = constantPool?.getOrNull(ref - 1)
            if (constant != null && constant is DataClassConstant) {
                val refsTo = constant.index
                val resultConstant = constantPool?.getOrNull(refsTo - 1)
                if (resultConstant != null && resultConstant is UTF8Constant) {
                    return resultConstant.value
                }
            }
        }
        return null
    }

    val mainClassName: String? = getMainOrSuperName(mainClassNameRef)

    val superClassName: String? = getMainOrSuperName(superClassNameRef)

    val interfacesHuman: List<String>? = if (interfacesRefs != null && constantPool != null) {
        val constants = interfacesRefs.map { constantPool[it - 1] }
        val res =
            constants.asSequence().filterIsInstance<DataClassConstant>().map { constantPool[it.index - 1] }
                .filterIsInstance<UTF8Constant>().map { it.value }.toList()
        if (res.size == constants.size) {
            res
        } else {
            null
        }
    } else {
        null
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    val accessFlags: List<String>? = when (accessFlagBin) {
        null -> null
        else -> {
            if (accessFlagBin.size != 2) throw IllegalArgumentException()
            val flagsMask = accessFlagBin.toInt()
            accessFlagsMap.entries.filter { entry -> entry.key and flagsMask != 0 }.map { entry -> entry.value }
                .toList()
        }
    }

    val fieldsHuman: List<Field>? = when (fieldsRaw) {
        null -> null
        else -> {
            if (constantPool != null) {
                fieldsRaw.map { Field(it, constantPool) }
            } else {
                null
            }
        }
    }

    val methodsHuman: List<Method>? = when (methodsRaw) {
        null -> null
        else -> {
            if (constantPool != null) {
                methodsRaw.map { Method(it, constantPool) }
            } else {
                null
            }
        }
    }

    val attributesHuman: List<String>? = when (attributesRaw) {
        null -> null
        else -> {
            if (constantPool != null) {
                val result =
                    attributesRaw.map { constantPool[it - 1] }.filterIsInstance<UTF8Constant>().map { it.value }
                if (result.size == attributesRaw.size) {
                    result
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

}


interface Constant

data class UTF8Constant(val value: String) : Constant
data class DataClassConstant(val index: Int) : Constant
data class OtherConstant(val tagIndex: Int) : Constant


@OptIn(ExperimentalUnsignedTypes::class)
operator fun Result.plus(other: Result): Result {
    return Result(
        magicNumber = this.magicNumber ?: other.magicNumber,
        minorVersion = this.minorVersion ?: other.minorVersion,
        majorVersion = this.majorVersion ?: other.majorVersion,
        accessFlagBin = this.accessFlagBin ?: other.accessFlagBin,
        mainClassNameRef = this.mainClassNameRef ?: other.mainClassNameRef,
        constantPool = this.constantPool ?: other.constantPool,
        superClassNameRef = this.superClassNameRef ?: other.superClassNameRef,
        interfacesRefs = this.interfacesRefs ?: other.interfacesRefs,
        fieldsRaw = this.fieldsRaw ?: other.fieldsRaw,
        methodsRaw = this.methodsRaw ?: other.methodsRaw,
        attributesRaw = this.attributesRaw ?: other.attributesRaw
    )
}

sealed class Either<out L : Any, out R : Any> private constructor(left: Any?, right: Any?) {
    abstract fun <T : Any> mapLeft(f: (L) -> T): Left<T>
    abstract fun <T : Any> mapRight(f: (R) -> T): Right<T>

    abstract fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U>
    abstract fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U>
}


data class Left<L : Any>(val value: L) : Either<L, Nothing>(value, null) {
    override fun <T : Any> mapLeft(f: (L) -> T) = Left(f(value))
    override fun <T : Any> mapRight(f: (Nothing) -> T): Nothing = throw IllegalStateException()

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (L, T) -> U): Left<U> = Left(f(value, other.value))
    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (Nothing, T) -> U): Right<U> =
        throw IllegalStateException()
}

data class Right<R : Any>(val value: R) : Either<Nothing, R>(null, value) {
    override fun <T : Any> mapLeft(f: (Nothing) -> T): Nothing = throw IllegalStateException()
    override fun <T : Any> mapRight(f: (R) -> T): Right<T> = Right(f(value))

    override fun <T : Any, U : Any> mergeLeft(other: Left<T>, f: (Nothing, T) -> U): Left<U> =
        throw IllegalStateException()

    override fun <T : Any, U : Any> mergeRight(other: Right<T>, f: (R, T) -> U): Right<U> = Right(f(value, other.value))
}

//R - good
interface Parsers {
    fun Location.toError(input: String) = ParseError(listOf(this to input))

    @OptIn(ExperimentalUnsignedTypes::class)
    fun <A : Any> run(p: Parser<A>, input: ByteSource): Either<ParseError, A>
}

fun String.addSStringAtIndex(str: String, index: Int) =
    StringBuilder(this).apply { insert(index, str) }.toString()

class ByteSource @OptIn(ExperimentalUnsignedTypes::class) constructor(public val source: UByteArray) {
    var startParsingFrom: Int = 0;
    var parsingNow: Int = 0;

    @OptIn(ExperimentalUnsignedTypes::class)
    fun hasNext(): Boolean = parsingNow < source.size

    @OptIn(ExperimentalUnsignedTypes::class)
    fun next(): UByte {
        parsingNow += 1
        return source[parsingNow - 1];
    }

    fun updateParsingStart() {
        startParsingFrom = parsingNow;
    }

    fun resetParsingNow() {
        parsingNow = startParsingFrom;
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun generateError(): String {
        val t = source.map { it.toString(16) }.toString().drop(1).dropLast(1).replace(",", "")
        return t.addSStringAtIndex("->", minOf(parsingNow * 3, t.length))
    }
}


fun parserPostprocessing(input: ByteSource, result: Any) {
    when (result) {
        is Left<*> -> input.resetParsingNow()
        is Right<*> -> input.updateParsingStart()
        else -> throw IllegalAccessException()
    }
}

fun parserPostprocessing(input: ByteSource, result: Any, saveInd: Int) {
    when (result) {
        is Left<*> -> {
            input.parsingNow = saveInd; input.startParsingFrom = saveInd
        }

        is Right<*> -> input.updateParsingStart()
        else -> throw IllegalAccessException()
    }
}

interface Combinators {
    fun <T : Any> or_(some: Parser<T>, other: Parser<T>) = { input: ByteSource ->
        val saveIndx = input.startParsingFrom;
        val firstValue = some(input)
        parserPostprocessing(input, firstValue, saveIndx)
        when (firstValue) {
            is Left -> {
                val secondValue = other(input)
                parserPostprocessing(input, secondValue, saveIndx)
                secondValue
            }

            else -> firstValue
        }
    }

    fun <T : Any, U : Any> seq_(first: Parser<T>, second: Parser<T>, merge: (T, T) -> U): Parser<U> =
        { input: ByteSource ->
            val firstValue = first(input)
            parserPostprocessing(input, firstValue)
            when (firstValue) {
                is Left -> firstValue
                is Right -> {
                    val secondValue = second(input)
                    parserPostprocessing(input, secondValue)
                    when (secondValue) {
                        is Left -> secondValue
                        is Right -> firstValue.mergeRight(secondValue) { a, b -> merge(a, b) }
                    }
                }
            }
        }

    fun <T : Any> repeat_(parser: Parser<T>, count: Int): Parser<List<T>> = { input: ByteSource ->
        run {
            val result = mutableListOf<T>()
            val saveInd = input.parsingNow
            repeat(count) {
                when (val resP = parser(input)) {
                    is Left -> {
                        parserPostprocessing(input, resP, saveInd)
                        return@run resP
                    }

                    is Right -> result.add(resP.value)
                }
            }
            Right(result)
        }
    }
}


open class DefaultParsers : Parsers, Combinators {

    @OptIn(ExperimentalUnsignedTypes::class)
    infix fun Parser<UByteArray>.seq(other: Parser<UByteArray>) = seq_(this, other) { a: UByteArray, b: UByteArray ->
        a + b
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    infix fun Parser<UByteArray>.or(other: Parser<UByteArray>) = or_(this, other)

    override fun <A : Any> run(p: Parser<A>, input: ByteSource): Either<ParseError, A> = p(input)

    @OptIn(ExperimentalUnsignedTypes::class)
    fun byte(expected: UByte): Parser<UByteArray> = { input: ByteSource ->
        when (input.hasNext()) {
            false -> Left(Location(input.generateError()).toError("Unexpected Empty Char"))
            else -> {
                when (val c = input.next()) {
                    expected -> Right(ubyteArrayOf(c))
                    else -> Left(Location(input.generateError()).toError("Unexpected char in byteparser"))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun pattern(expected: UByteArray): Parser<UByteArray> = { input: ByteSource ->
        if (expected.isEmpty()) {
            throw IllegalArgumentException()
        } else {
            val parser = expected.map { byte(it) }.reduce { res, current -> res seq current };
            when (parser(input)) {
                is Right -> Right(expected);
                else -> Left(Location(input.generateError()).toError("Unexpected string pattern"))
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun betweenByte(from: UByte, to: UByte): Parser<UByteArray> = { input: ByteSource ->
        if (input.hasNext()) {
            when (val c = input.next()) {
                in from..to -> Right(ubyteArrayOf(c))
                else -> Left(Location(input.generateError()).toError("Unecpected char"))
            }
        } else {
            Left(Location(input.generateError()).toError("Unexpected end of input"))
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun anyByte(): Parser<UByteArray> = betweenByte(0x00u, 0xFFu)

    @OptIn(ExperimentalUnsignedTypes::class)
    fun anyTwoBytes(): Parser<UByteArray> = anyByte() seq anyByte()

    @OptIn(ExperimentalUnsignedTypes::class)
    fun between(minv: UByteArray, maxv: UByteArray): Parser<UByteArray> = { input: ByteSource ->
        if (minv.isEmpty() || maxv.isEmpty() || maxv.size != minv.size) {
            throw IllegalArgumentException()
        } else {
            minv.zip(maxv).map { p -> betweenByte(p.first, p.second) }.reduce { acc, current -> acc seq current }(input)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun end(): Parser<UByteArray> = {
        if (it.hasNext()) {
            Left(Location(it.generateError()).toError("Expected end of char source"))
        } else {
            Right(ubyteArrayOf())
        }
    }
}

class ExtendedParsers : DefaultParsers() {
    @OptIn(ExperimentalUnsignedTypes::class)
    private val MAGIC_NUMBER_PATTERN = ubyteArrayOf(0xCAu, 0xFEu, 0xBAu, 0xBEu);

    infix fun Parser<Result>.seqR(other: Parser<Result>) = seq_(this, other) { a: Result, b: Result ->
        a + b
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseMagic(): Parser<Result> = { input: ByteSource ->
        val p = pattern(MAGIC_NUMBER_PATTERN)
        when (val res = p(input)) {
            is Left -> res
            is Right -> Right(Result(magicNumber = true))
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseMinorVersion(): Parser<Result> = { input: ByteSource ->
        val p = between(ubyteArrayOf(0x00u, 0x00u), ubyteArrayOf(0xFFu, 0xFFu))
        when (val r = p(input)) {
            is Right -> Right(Result(minorVersion = r.value.toInt()))

            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseMajorVersion(): Parser<Result> = { input: ByteSource ->
        val p = between(ubyteArrayOf(0x00u, 0x2Du), ubyteArrayOf(0x00u, 0x3Fu))
        when (val r = p(input)) {
            is Right -> Right(Result(majorVersion = r.value.toInt()))

            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseUTF8String(byteLenght: Int): Parser<String> = { input: ByteSource ->
        val parser = List(byteLenght) { anyByte() }.reduce { acc, c -> acc seq c }(input)
        when (parser) {
            is Right -> {
                val resultString = parser.value.toByteArray().toString(Charsets.UTF_8)
                Right(resultString)
            }

            is Left -> parser
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseUTF8Constant(): Parser<Constant> = { input: ByteSource ->
        val lengthParser = anyTwoBytes()
        when (val len = lengthParser(input)) {
            is Left -> len
            is Right -> {
                val lenI = len.value.map { it.toString(16) }.reduce { acc, current -> acc + current }.toInt(16)
                val pString = parseUTF8String(lenI)
                when (val str = pString(input)) {
                    is Left -> str
                    is Right -> Right(UTF8Constant(str.value))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseDataClassConstant(): Parser<Constant> = { input: ByteSource ->
        when (val p = (anyByte() seq anyByte())(input)) {
            is Left -> p
            is Right -> Right(DataClassConstant(p.value.map { it.toString(16) }.reduce { acc, current -> acc + current }
                .toInt(16)))
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseOtherConstant(parsersRepeat: Int, tagIndex: Int): Parser<Constant> = { input: ByteSource ->
        val p = List(parsersRepeat) { anyByte() }.reduce { acc, current -> acc seq current }
        when (val res = p(input)) {
            is Left -> res
            is Right -> Right(OtherConstant(tagIndex))
        }
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseConstant(): Parser<Constant> = { input: ByteSource ->
        val pTag = betweenByte(0x01u, 0x14u);
        when (val restag = pTag(input)) {
            is Left -> restag
            is Right -> {
                val restagVal = restag.value.map { it.toString(16) }.reduce { acc, current -> acc + current }.toInt(16)
                when (restagVal) {
                    1 -> parseUTF8Constant()(input)
                    7 -> parseDataClassConstant()(input)
                    8, 16, 19, 20 -> parseOtherConstant(2, restagVal)(input)
                    9, 10, 11, 3, 4, 12, 17, 18 -> parseOtherConstant(4, restagVal)(input)
                    5, 6 -> parseOtherConstant(8, restagVal)(input)
                    15 -> parseOtherConstant(3, restagVal)(input)
                    else -> Left(Location(input.generateError()).toError("Unexpected char at const pool"))
                }
            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseConstantPool(): Parser<Result> = { input: ByteSource ->
        val p = between(ubyteArrayOf(0x00u, 0x00u), ubyteArrayOf(0xFFu, 0xFFu))
        when (val r = p(input)) {
            is Right -> {
                val n = r.value.map { it.toString(16) }.reduce { acc, current -> acc + current }.toInt(16)
                val skipParser = repeat_(parseConstant(), n - 1)
                val rr = skipParser(input)

                when (rr) {
                    is Right -> Right(Result(constantPool = rr.value))
                    is Left -> rr
                }
            }

            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseAccessFlags(): Parser<Result> = { input: ByteSource ->
        val p = anyByte() seq anyByte()
        val r = p(input)
        when (r) {
            is Right -> Right(Result(accessFlagBin = r.value))
            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseInterface(): Parser<UByteArray> = anyByte() seq anyByte()

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseInterfaces(): Parser<Result> = {
        val pCount = anyByte() seq anyByte()
        when (val nR = pCount(it)) {
            is Left -> nR
            is Right -> {
                val n = nR.value.toInt()
                when (val pInterfaces = repeat_(parseInterface(), n)(it)) {
                    is Left -> pInterfaces
                    is Right -> {
                        val resInterfacesList = pInterfaces.value.map { e ->
                            e.toInt()
                        }
                        Right(Result(interfacesRefs = resInterfacesList))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseAttribute(): Parser<Int> = {
        val twoBytesParser = anyTwoBytes()
        when (val name = twoBytesParser(it)) {
            is Left -> name
            is Right -> {
                val lenght = twoBytesParser seq twoBytesParser
                when (val size = lenght(it)) {
                    is Left -> size
                    is Right -> {
                        val info = repeat_(anyByte(), size.value.toInt())
                        when (val infoRes = info(it)) {
                            is Left -> infoRes
                            is Right -> Right(name.value.toInt())
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseField(): Parser<List<Int>> = { it ->
        val twoBytesParser = anyByte() seq anyByte()
        val resParser = repeat_(twoBytesParser, 4)
        when (val res1 = resParser(it)) {
            is Left -> res1
            is Right -> {
                val attributeCount = res1.value[3].toInt()
                val attributes = repeat_(parseAttribute(), attributeCount)
                when (val attributesRes = attributes(it)) {
                    is Left -> attributesRes
                    is Right -> {
                        val newList = res1.value.map { it.toInt() }.toMutableList()
                        newList.addAll(attributesRes.value)
                        Right(newList)
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseFields(): Parser<Result> = {
        val pCount = anyTwoBytes()
        when (val nR = pCount(it)) {
            is Left -> nR
            is Right -> {
                val n = nR.value.toInt()
                when (val fieldsRes = repeat_(parseField(), n)(it)) {
                    is Left -> fieldsRes
                    is Right -> Right(Result(fieldsRaw = fieldsRes.value))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseMethods(): Parser<Result> = {
        val pCount = anyTwoBytes()
        when (val nR = pCount(it)) {
            is Left -> nR
            is Right -> {
                val n = nR.value.toInt()
                when (val methodsRes = repeat_(parseField(), n)(it)) {
                    is Left -> methodsRes
                    is Right -> Right(Result(methodsRaw = methodsRes.value))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseClassName(): Parser<Result> = { input: ByteSource ->
        val p = anyTwoBytes()
        when (val r = p(input)) {
            is Right -> {
                Right(Result(mainClassNameRef = r.value.map { it.toString(16) }.reduce { acc, current -> acc + current }
                    .toInt(16)))
            }

            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseSuperClassName(): Parser<Result> = { input: ByteSource ->
        val p = anyTwoBytes()
        when (val r = p(input)) {
            is Right -> {
                Right(Result(superClassNameRef = r.value.map { it.toString(16) }
                    .reduce { acc, current -> acc + current }.toInt(16)))
            }

            is Left -> r
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun parseAttributesGlobal(): Parser<Result> = {
        val p = anyTwoBytes()
        when (val nR = p(it)) {
            is Left -> nR
            is Right -> {
                val attributeCount = nR.value.toInt()
                when (val attributeNames = repeat_(parseAttribute(), attributeCount)(it)) {
                    is Left -> attributeNames
                    is Right -> Right(Result(attributesRaw = attributeNames.value))
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun expectEnd(): Parser<Result> = {
        val p = end()
        when (val res = p(it)) {
            is Left -> res
            is Right -> Right(Result())
        }
    }

    fun parseJVM(): Parser<Result> =
        parseMagic() seqR parseMinorVersion() seqR parseMajorVersion() seqR parseConstantPool() seqR parseAccessFlags() seqR parseClassName() seqR parseSuperClassName() seqR parseInterfaces() seqR parseFields() seqR parseMethods() seqR parseAttributesGlobal() seqR expectEnd()

}

data class Location(val input: String)
data class ParseError(val stack: List<Pair<Location, String>>);

typealias Parser<A> = (ByteSource) -> Either<ParseError, A>
