package homework04

import homework04.parseUtils.*
import homework04.types.*
import java.io.File


class ClassFileParser : ByteParsers(), CharParsers {

    private fun versionParser(): Parser<ByteArrayView, String> = seq(twoByteParser, twoByteParser)
    { minor, major -> bytesToInt(major).toString() + "." + bytesToInt(minor).toString() }

    private val constantSizes = mapOf(
        3 to 4, 4 to 4, 5 to 8, 6 to 8, 8 to 2, 9 to 4, 10 to 4, 11 to 4,
        12 to 4, 15 to 3, 16 to 2, 17 to 4, 18 to 4, 19 to 2, 20 to 2
    )

    private fun classInfoParser(): Parser<ByteArrayView, Constant> =
        map(twoByteParser) { ClassInfoConstant(bytesToInt(it)) }

    private fun utfParser(): Parser<ByteArrayView, Constant> =
        map(composition(twoByteParser, ::nByte) { bytesToInt(it) }) { UTFConstant(bytesToString(it)) }

    private fun getConstantParser(tag: Int): Parser<ByteArrayView, Constant> =
        when (tag) {
            1 -> utfParser()
            7 -> classInfoParser()
            else -> map(nByte(constantSizes[tag] ?: 0)) { OtherConstant(tag) }
        }

    private fun constantParser(): Parser<ByteArrayView, Constant> =
        composition(anyByte(), ::getConstantParser) { bytesToInt(ByteArray(0).plus(it)) }

    private fun constantPoolParser(n: Int): Parser<ByteArrayView, List<Constant>> = repeatParser(constantParser(), n)

    private inline fun <reified T> accessFlagsParser(): Parser<ByteArrayView, List<T>>
            where T : Enum<T>, T : AccessFlags<T> =
        map(twoByteParser) { mask ->
            enumValues<T>().mapNotNull { if ((it.code and bytesToInt(mask)) == 0) null else it }
        }

    private fun interfacesParser(n: Int): Parser<ByteArrayView, List<Int>> =
        map(repeatParser(twoByteParser, n)) { list -> list.map { bytesToInt(it) } }

    private fun attributeParser(): Parser<ByteArrayView, Int> =
        seq(twoByteParser, composition(fourByteParser, ::nByte, ::bytesToInt)) { a, _ -> bytesToInt(a) }

    private fun attributesParser(n: Int): Parser<ByteArrayView, List<Int>> = repeatParser(attributeParser(), n)

    private inline fun <reified T> fieldOrMethodParser(): Parser<ByteArrayView, FieldOrMethodParsed>
            where T : AccessFlags<T>, T : Enum<T> =
        seq(
            accessFlagsParser<T>(),
            twoByteParser,
            twoByteParser,
            composition(twoByteParser, ::attributesParser, ::bytesToInt)
        )
        { list ->
            FieldOrMethodParsed(
                list[0] as List<AccessFlags<*>>,
                bytesToInt(list[1] as ByteArray),
                bytesToInt(list[2] as ByteArray),
                list[3] as List<Int>
            )
        }

    private fun fieldsParser(n: Int): Parser<ByteArrayView, List<FieldOrMethodParsed>> =
        repeatParser(fieldOrMethodParser<FieldAccessFlags>(), n)

    private fun methodsParser(n: Int): Parser<ByteArrayView, List<FieldOrMethodParsed>> =
        repeatParser(fieldOrMethodParser<MethodAccessFlags>(), n)

    private val fieldDescriptorMatch =
        mapOf(
            'B' to "byte",
            'C' to "char",
            'D' to "double",
            'F' to "float",
            'I' to "int",
            'J' to "long",
            'S' to "short",
            'Z' to "boolean"
        )

    private fun fieldDescriptorParser(): Parser<StringView, String> =
        map(charSet(fieldDescriptorMatch.keys)) { fieldDescriptorMatch[it]!! } or
                seq(
                    char('L'),
                    repeatUntilFailure(charNotSet(setOf(';'))),
                    char(';')
                ) { list -> (list[1] as List<*>).joinToString("") } or
                seq(
                    repeatUntilFailure(char('[')),
                    charSet(fieldDescriptorMatch.keys)
                ) { a, b -> fieldDescriptorMatch[b] + "[]".repeat(a.size) }

    private fun fieldDescriptorConverter(descriptor: String): String =
        when (val result = run(fieldDescriptorParser(), StringView(descriptor))) {
            is Failure -> ""
            is Success -> result.a
        }

    private fun methodDescriptorParser(): Parser<StringView, String> =
        seq(
            char('('),
            repeatUntilFailure(fieldDescriptorParser()),
            char(')'),
            (fieldDescriptorParser() or map(char('V')) { "Void" })
        ) { list -> "Return type: ${list[3]}; Parameters types: ${(list[1] as List<*>).joinToString(", ")}" }

    private fun methodDescriptorConverter(descriptor: String): String =
        when (val result = run(methodDescriptorParser(), StringView(descriptor))) {
            is Failure -> ""
            is Success -> result.a
        }

    private fun isClassFile(input: ByteArrayView): Boolean {
        return run(fourByteParser, input) is Success
    }

    @Suppress("UNCHECKED_CAST")
    private val classFileParser =
        seq(
            fourByteParser,
            versionParser(),
            composition(twoByteParser, ::constantPoolParser) { bytesToInt(it) - 1 },
            accessFlagsParser<ClassAccessFlags>(),
            map(twoByteParser, ::bytesToInt),
            map(twoByteParser, ::bytesToInt),
            composition(twoByteParser, ::interfacesParser, ::bytesToInt),
            composition(twoByteParser, ::fieldsParser, ::bytesToInt),
            composition(twoByteParser, ::methodsParser, ::bytesToInt),
            composition(twoByteParser, ::attributesParser, ::bytesToInt)
        )
        { result: List<Any> ->
            val version = result[1] as String
            val pool = result[2] as List<Constant>
            val accessFlags = result[3] as List<ClassAccessFlags>
            val className = (pool[(pool[result[4] as Int - 1] as ClassInfoConstant).nameIndex - 1] as UTFConstant).name
            val superName = if (result[5] as Int == 0) ""
            else (pool[(pool[result[5] as Int - 1] as ClassInfoConstant).nameIndex - 1] as UTFConstant).name
            val interfaces =
                (result[6] as List<Int>).map { (pool[(pool[it - 1] as ClassInfoConstant).nameIndex - 1] as UTFConstant).name }
            val fields = (result[7] as List<FieldOrMethodParsed>).map {
                Field(
                    (pool[it.nameIndex - 1] as UTFConstant).name,
                    fieldDescriptorConverter((pool[it.descriptorIndex - 1] as UTFConstant).name),
                    it.accessFlags as List<FieldAccessFlags>,
                    it.attributesNamesIndexes.map { index -> (pool[index - 1] as UTFConstant).name }
                )
            }
            val methods = (result[8] as List<FieldOrMethodParsed>).map {
                Method(
                    (pool[it.nameIndex - 1] as UTFConstant).name,
                    methodDescriptorConverter((pool[it.descriptorIndex - 1] as UTFConstant).name),
                    it.accessFlags as List<MethodAccessFlags>,
                    it.attributesNamesIndexes.map { index -> (pool[index - 1] as UTFConstant).name }
                )
            }
            val attributes = (result[9] as List<Int>).map { index -> (pool[index - 1] as UTFConstant).name }
            ClassFile(version, accessFlags, className, superName, interfaces, fields, methods, attributes)
        }

    fun parse(input: ByteArray): Result<ByteArrayView, ClassFile> {
        val inputView = ByteArrayView(input)
        if (!isClassFile(inputView)) {
            return Failure(ByteLocation(inputView, 0).toError("Due to magic absence it's not a ClassFile"))
        }
        return run(classFileParser, inputView)
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Execute with filename")
        return
    }
    val file = File(args[0])
    if (!file.isFile) {
        println("Given file doesn't exist")
        return
    }
    val parser = ClassFileParser()
    when (val result = parser.parse(file.readBytes())) {
        is Failure -> println("Parse error: " + result.get)
        is Success -> {
            println("ClassFile version: ${result.a.version}")
            println("Access modifiers: ${result.a.accessFlags.joinToString(", ")}")
            println("Class name: ${result.a.className}")
            println("Super class name: ${result.a.superName}")
            println("Interfaces: ${result.a.interfaces.joinToString(", ")}")
            println("Fields: ")
            result.a.fields.forEach {
                println("Name: ${it.name}")
                println("Type: ${it.type}")
                println("Access modifiers: ${it.accessFlags.joinToString(", ")}")
                println("Attributes: ${it.attributes.joinToString(", ")}")
                println()
            }
            println("Methods: ")
            result.a.methods.forEach {
                println("Name: ${it.name}")
                println("Type: ${it.type}")
                println("Access modifiers: ${it.accessFlags.joinToString(", ")}")
                println("Attributes: ${it.attributes.joinToString(", ")}")
                println()
            }
            println("Attributes: ${result.a.attributes.joinToString(", ")}")
        }
    }
}
