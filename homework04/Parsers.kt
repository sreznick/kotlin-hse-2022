data class Location(val input: String, val offset: Int = 0) {
    private val slice by lazy { input.slice(0..offset + 1) }
    val line by lazy { slice.count { it == '\n' } + 1 }
    val column by lazy {
        when (val n = slice.lastIndexOf('\n')) {
            -1 -> offset + 1
            else -> offset - n
        }
    }
}

sealed class Result<out A>
data class Success<out A>(val value: A, val consumed: Int) : Result<A>()
data class Failure(val get: ParseError, val isCommitted: Boolean) : Result<Nothing>()
data class ParseError(val stack: List<Pair<Location, String>>)
typealias Parser<A> = (Location) -> Result<A>

infix fun <T> T.cons(la: List<T>): List<T> = listOf(this) + la

internal fun Location.toError(msg: String): ParseError = ParseError(listOf(this to msg))

fun ParseError.push(loc: Location, msg: String): ParseError =
    this.copy(stack = (loc to msg) cons this.stack)

fun <A> Result<A>.mapError(f: (ParseError) -> ParseError): Result<A> =
    when (this) {
        is Success -> this
        is Failure -> Failure(f(this.get), this.isCommitted)
    }

fun <A> Result<A>.uncommit(): Result<A> =
    when (this) {
        is Failure ->
            if (this.isCommitted)
                Failure(this.get, false)
            else this
        is Success -> this
    }

fun <A> Failure.joinErrors(result: Result<A>): Result<A> =
    when (result) {
        is Failure -> Failure(ParseError(this.get.stack + result.get.stack),
            this.isCommitted || result.isCommitted )
        is Success -> result
    }

fun ParseError.tag(msg: String): ParseError {
    val latest = this.stack.last()
    val latestLocation = latest.first
    return ParseError(listOf(latestLocation to msg))
}

fun Location.advanceBy(n: Int): Location =
    this.copy(offset = this.offset + n)

fun Location.hasInput(n: Int = 0): Boolean =
    this.offset + n < this.input.length

fun Location.skipBy(n: Int) = this.offset + n

fun <A> Result<A>.addCommit(commit: Boolean): Result<A> =
    when (this) {
        is Failure ->
            Failure(this.get, this.isCommitted || commit)
        is Success -> this
    }

fun <A> Result<A>.advanceSuccess(n: Int): Result<A> =
    when (this) {
        is Success ->
            Success(this.value, this.consumed + n)
        is Failure -> this
    }



interface Parsers {
    fun <A> attempt(p: Parser<A>): Parser<A> = { s -> p(s).uncommit() }

    fun <A> scope(msg: String, pa: Parser<A>): Parser<A> =
        { state -> pa(state).mapError { pe -> pe.push(state, msg) } }

    fun <A> tag(msg: String, pa: Parser<A>): Parser<A> =
        { state ->
            pa(state).mapError { pe ->
                pe.tag(msg)
            }
        }

    fun <A> run(p: Parser<A>, input: String): Result<A> = p(Location(input))
}

interface Combinators: Parsers {
    fun <A> or(pa: Parser<A>, pb: () -> Parser<A>): Parser<A> =
        { state ->
            when (val r: Result<A> = pa(state)) {
                is Failure -> pb()(state)
                is Success -> r
            }
        }
    fun <A, B> map(pa: Parser<A>, f: (A) -> B): Parser<B> =
        { state ->
            when (val r: Result<A> = pa(state)) {
                is Failure -> r
                is Success -> Success(f(r.value), r.consumed)
            }
        }

    fun <A, B> mapWithError(pa: Parser<A>, msg: String, f: (A) -> B): Parser<B> =
        { state ->
            when (val r: Result<A> = pa(state)) {
                is Failure -> r
                is Success -> {
                    try {
                        Success(f(r.value), r.consumed)
                    } catch (e: Exception) {
                        Failure(state.toError(msg), false)
                    }
                }
            }
        }
    
    fun <A, B, C> seq(pa: Parser<A>, pb: () -> Parser<B>, merge: (A, B) -> C): Parser<C> =
        { state: Location ->
            when (val r1: Result<A> = pa(state)) {
                is Failure -> r1
                is Success -> {
                    when (val r2 = pb()(state.advanceBy(r1.consumed))) {
                        is Failure -> r2
                        is Success -> Success(merge(r1.value, r2.value), r1.consumed + r2.consumed)}
                }
            }
        }

    fun <A, B> seqn(n: Int, pa: () -> Parser<A>, start: B, merge: (B, A) -> B): Parser<B> =
        { state: Location ->
            if (n == 0) Success(start, 0)
            else {
                when (val r1 = pa()(state)) {
                    is Failure -> r1
                    is Success -> {
                        when (val r2 =
                            seqn(n - 1, pa, merge(start, r1.value), merge) (state.advanceBy(r1.consumed))) {
                            is Failure -> r2
                            is Success -> Success(r2.value, r1.consumed + r2.consumed)
                        }
                    }
                }
            }
        }

    fun <A, B> flatMap(pa: Parser<A>, f: (A) -> Parser<B>): Parser<B> =
        { state ->
            when (val result = pa(state)) {
                is Success ->
                    f(result.value)(state.advanceBy(result.consumed))
                        .addCommit(result.consumed != 0)
                        .advanceSuccess(result.consumed)
                is Failure -> result
            }
        }

    fun <A, B> flatMapWithError(pa: Parser<A>, msg: String, f: (A) -> Parser<B>): Parser<B> =
        { state ->
            when (val result = pa(state)) {
                is Failure -> result
                is Success -> {
                    try {
                        f(result.value)(state.advanceBy(result.consumed))
                            .addCommit(result.consumed != 0)
                            .advanceSuccess(result.consumed)
                    } catch (e: Exception) {
                        Failure(state.toError(msg), false)
                    }
                }
            }
        }

    fun <A, B> star(pa: Parser<A>, start: B, merge: (B, A) -> B, consumed: Int = 0): Parser<B> =
        { state: Location ->
            when (val r: Result<A> = pa(state)) {
                is Failure -> Success(start, consumed)
                is Success -> star(pa, merge(start, r.value), merge,
                    consumed + r.consumed)(state.advanceBy(r.consumed))
            }
        }

    fun <A> question(pa: Parser<A>, default: A): Parser<A> =
        { state: Location ->
            when (val r: Result<A> = pa(state)) {
                is Failure -> Success(default, 0)
                is Success -> r
            }
        }
}

interface CharParsers {
    fun char(c: Char): Parser<Char> =
        { state: Location ->
            when {
                !state.hasInput() -> Failure(state.toError("Index out of range"), false)
                state.input[state.offset] == c -> Success(c, 1)
                else -> Failure(state.toError("Expected: $c"), false)
            }
        }

    fun charRange(from: Char, to: Char): Parser<Char> =
        { state: Location ->
            when {
                !state.hasInput() -> Failure(state.toError("Index out of range"), false)
                state.input[state.offset] in from .. to -> Success(state.input[state.offset], 1)
                else -> Failure(state.toError("Expected: $from - $to"), false)
            }
        }

    fun butChar(c: Char): Parser<Char> =
        { state: Location ->
            when {
                !state.hasInput() -> Failure(state.toError("Index out of range"), false)
                state.input[state.offset] != c -> Success(state.input[state.offset], 1)
                else -> Failure(state.toError("Expected: not $c"), false)
            }
        }

    fun string(s: String, index: Int = 0): Parser<String> =
    { state: Location ->
        if (index >= s.length)
                Success(s, s.length)
        else {
            when (val r = char(s[index]) (state)) {
                is Failure -> r
                is Success -> string(s, index + 1) (state.advanceBy(1))
            }
        }
    }
}



abstract class StructureHTML {
    protected fun toStringImpl(marker: String, inside: String): String = "<$marker>$inside</$marker>"
}

class Body(private val inside: List<StructureHTML>) : StructureHTML() {
    override fun toString(): String = toStringImpl("body", inside.joinToString("") { it.toString() })
}

class Division(private val inside: List<StructureHTML>) : StructureHTML() {
    override fun toString(): String = toStringImpl("div", inside.joinToString("") { it.toString() })
}

class Paragraph(private val text: String) : StructureHTML() {
    override fun toString(): String = toStringImpl("p", text)
}

class HTMLParser: Parsers, Combinators, CharParsers {

    fun body(): Parser<Body> =
        seq(question(string("<body>"), ""),
            { seq(internal(), { question(string("</body>"), "") }) { int, _ -> Body(int) } })
        { _, bod -> bod }

    private fun internal(): Parser<List<StructureHTML>> =
        star(or(division()) { paragraph() }, emptyList(), { list, struct -> list + struct})

    private fun division(): Parser<Division> =
        seq(string("<div>"),
            { seq(internal(), { question(string("</div>"), "") }) { int, _ -> Division(int) } })
        { _, div -> div }

    private fun paragraph(): Parser<Paragraph> =
        or(seq(string("<p>"),
            { seq(question(textHTML(), ""), { question(string("</p>"), "") })
                                                    {text, _ -> Paragraph(text) } }) {_, p -> p}
        ) { map(textHTML())
            { text -> Paragraph(text) } }

    private fun textHTML(): Parser<String> =
         seq(butChar('<'), { star(butChar('<'), StringBuilder(), {s, c -> s.append(c)}) },
             {c, s -> s.insert(0, c).toString()})
}

open class Utf8Parser: Parsers, CharParsers, Combinators {
    private val types: Map<Char, String> = mapOf(Pair('B', "byte"), Pair('C', "char"),
    Pair('D', "double"), Pair('F', "float"), Pair('I', "int"), Pair('J', "long"), Pair('S', "short"),
    Pair('Z', "boolean"))

    private fun primitiveClass(): Parser<String> = mapWithError(butChar('['), "Impossible parameter type",
        {c -> types.getValue(c) })

    private fun customClass(): Parser<String> = seq(char('L'), { seq(star(butChar(';'), "", {s, c -> s + c}),
        { char(';') }, {s, _ -> s}) }, {_, s -> s})

    private fun className(): Parser<String> = or(customClass(), { primitiveClass() })

    private fun arrayDimension(): Parser<String> = seq(char('['), { seq(star(char('['), 0, {n, _, -> n + 1}),
        { className() }, {n, s -> Pair(n, s)}) }, {_, p -> p.second + "[]".repeat(p.first + 1) })

    fun field(): Parser<String> = or(className(), { arrayDimension() })

    fun method(): Parser<Pair<String, String>> = map(seq(char('('), { seq(star(field(), emptyList<String>(),
        {list, f -> list + f}),
        { seq(char(')'), { or(char('V'), { field() }) }, {_, r -> r}) }, { list, r -> list + r }
    ) }, {_, list -> list } ), { list -> Pair((if (list.last().toString() == "V") "void" else list.last()).toString(),
            "(" + list.dropLast(1).joinToString(separator = ", ") + ")") })
}

interface ConstantPoolItem

data class Class(val tag: Int, val name_index: Int) : ConstantPoolItem // 7, 19, 20
data class Utf8(val string: String) : ConstantPoolItem {// 1
    override fun toString(): String = string
}
data class Ref(val tag: Int, val class_index: Int, val name_and_type_index: Int) : ConstantPoolItem // 9; 10; 11
data class NameAndType(val name_index: Int, val descriptor_index: Int) : ConstantPoolItem // 12
data class Attribute(val attribute_name_index: Int)
data class Info(val access_flags: Int, val name_index: Int, val descriptor_index: Int, val attributes: List<Attribute>)
class ClassFile(val version: String, val constant_pool: List<ConstantPoolItem?>, val access_flags: Int,
val this_class: Int, val super_class: Int, val interfaces: List<Int>, val fields: List<Info>, val methods: List<Info>,
val attributes: List<Attribute>) {
    private fun getClassName(index: Int): String =
        (constant_pool[(constant_pool[index - 1] as Class).name_index - 1] as Utf8).toString()

    private fun getUtf8(index: Int): String = (constant_pool[index - 1] as Utf8).toString()

    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        val parser = Utf8Parser()

        sb.append("Class file\n\n")
        sb.append("Version: $version\n\n")
        sb.append("Access flags: ${parseFlags(access_flags, CLASS_FLAGS)}\n\n")
        sb.append("Class name: ${getClassName(this_class)}\n\n")
        sb.append("Super class name: ${getClassName(super_class)}\n\n")
        if (interfaces.isNotEmpty()) {
            sb.append("Interfaces:\n")
            for (index in interfaces) {
                sb.append("${getClassName(index)}\n")
            }
            sb.append("\n")
        }
        if (fields.isNotEmpty()) {
            sb.append("Fields:\n")
            for (field in fields) {
                sb.append(parseFlags(field.access_flags, FIELD_FLAGS))
                sb.append(" ${(parser.run(parser.field(), getUtf8(field.descriptor_index)) as Success).value} ")
                sb.append("${getUtf8(field.name_index)} ( ")
                for (attr in field.attributes) {
                    sb.append("${getUtf8(attr.attribute_name_index)} ")
                }
                sb.append(")\n")
            }
            sb.append("\n")
        }
        if (methods.isNotEmpty()) {
            sb.append("Methods:\n")
            for (method in methods) {
                sb.append(parseFlags(method.access_flags, METHOD_FLAGS))
                val desc = (parser.run(parser.method(), getUtf8(method.descriptor_index)) as Success).value
                sb.append("${desc.first} ${getUtf8(method.name_index)}")
                sb.append("${desc.second} ( ")
                for (attr in method.attributes) {
                    sb.append("${getUtf8(attr.attribute_name_index)} ")
                }
                sb.append(")\n")
            }
            sb.append("\n")
        }
        if (attributes.isNotEmpty()) sb.append("Attributes:\n")
        for (attr in attributes) {
            sb.append(getUtf8(attr.attribute_name_index))
            sb.append("\n")
        }
        return sb.toString()
    }

    private val CLASS_FLAGS: List<Pair<Int, String>> = listOf(Pair(1, "public "), Pair(16, "final "), Pair(32, "super "),
        Pair(512, "interface "), Pair(1024, "abstract"), Pair(4096, "synthetic "), Pair(8192, "annotation "),
        Pair(16384, "enum "), Pair(16384, "enum "))

    private val FIELD_FLAGS: List<Pair<Int, String>> = listOf(Pair(1, "public "), Pair(2, "private "),
        Pair(4, "protected "), Pair(8, "static "), Pair(16, "final "), Pair(64, "volatile "), Pair(128, "transient "),
        Pair(4096, "synthetic "), Pair(32768, "module "))

    private val METHOD_FLAGS: List<Pair<Int, String>> = listOf(Pair(1, "public "), Pair(2, "private "),
        Pair(4, "protected "), Pair(8, "static "), Pair(16, "final "), Pair(32, "synchronized "), Pair(64, "bridge "),
        Pair(128, "varargs"), Pair(256, "native "), Pair(1024, "abstract "), Pair(2048, "strict "),
        Pair(4096, "synthetic "))

    private fun parseFlags(flags: Int, list: List<Pair<Int, String>>): String {
        val sb = StringBuilder()
        for (p in list) {
            if ((flags and p.first) == p.first) sb.append(p.second)
        }
        return sb.toString()
    }
}

class JVMByteCodeParser: Utf8Parser() {
    private val MAGIC_NUMBER: String = "Êþº¾"

    private val SKIP_CONSTANT_POOL: Map<Int, Int> = mapOf(Pair(8, 2), Pair(3, 4), Pair(4, 4), Pair(5, 8),
        Pair(6, 8), Pair(15, 3), Pair(16, 2), Pair(17, 4), Pair(18, 4))



    private fun tag(t1: Int, t2: Int, t3: Int) : Parser<Int> =
        map(or(char(t1.toChar()), { or(char(t2.toChar()), { char(t3.toChar()) }) }), { it.code })

    private fun utf8() : Parser<Utf8> =
        seq(char(1.toChar()),  { flatMap(readBytes(2),
            { str -> readBytes(str.toInt2()) }) }, { _, str -> Utf8(str) })

    private fun classInterface() : Parser<Class> =
        seq(
            tag(7, 19, 20), { readBytesToInt2()  },
            { tag, index -> Class(tag, index) })

    private fun ref() : Parser<Ref> =
        seq(
            tag(9, 10, 11),
            { seq(readBytesToInt2(), { readBytesToInt2() }, {i1, i2 -> Pair(i1, i2) } )},
            {tag, p -> Ref(tag, p.first, p.second) } )

    private fun nameAndType() : Parser<NameAndType> =
        seq(char(12.toChar()),
            { seq(readBytesToInt2(), { readBytesToInt2() }, { i1, i2 -> Pair(i1, i2)}) },
            {_, p -> NameAndType(p.first, p.second)})

    private fun otherConstantPoolItem(): Parser<ConstantPoolItem?> =
        map(flatMapWithError(map(readBytes(1), { it[0].code }),
           "Impossible tag", { n -> readBytes(SKIP_CONSTANT_POOL.getValue(n)) }), { null })

    private fun constantPoolItem() : Parser<ConstantPoolItem?> =
        or(classInterface(), { or(utf8(), { or(ref(),
            { or(nameAndType(), { otherConstantPoolItem() })}) } ) })

    private fun constantPool(constant_pool_count: Int): Parser<List<ConstantPoolItem?>> =
        seqn(constant_pool_count - 1, { constantPoolItem() }, emptyList(),
            {list, el -> list + el})

    private fun readBytes(n: Int): Parser<String> =
        { state: Location ->
            when {
                !state.hasInput(n - 1) -> Failure(state.toError("Index out of range"), false)
                else -> Success(state.input.substring(state.offset, state.offset + n), n)
            }
        }

    private fun readBytesToInt2(): Parser<Int> = map(readBytes(2)) { it.toInt2() }

    private fun magic(): Parser<String> = tag("The file is not a class file", string(MAGIC_NUMBER))

    private fun version(): Parser<String> = seq(readBytes(2), { readBytes(2) },
        {m, M ->
            M.toInt2().toString() + "." + m.toInt2() })

    fun String.toInt2(): Int {
        assert(this.length == 2)
        return (this[0].code shl 8) + this[1].code
    }

    fun String.toInt4(): Int {
        assert(this.length == 4)
        return (this[0].code shl 24) + (this[1].code shl 16) + (this[2].code shl 8) + this[3].code
    }

    private fun this_class(): Parser<Int> = readBytesToInt2()
    private fun super_class(): Parser<Int> = readBytesToInt2()
    private fun access_flags(): Parser<Int> = readBytesToInt2()
    private fun count(): Parser<Int> = readBytesToInt2()

    private fun interfaces(n: Int): Parser<List<Int>> = seqn(n, { readBytesToInt2() } , emptyList(),
        {list, i -> list + i})

    private fun attribute(): Parser<Attribute> = seq(readBytesToInt2(),
        { flatMap(map(readBytes(4), { it.toInt4() }), { n -> readBytes(n) } ) },
        {name, _ -> Attribute(name)})

    private fun info(): Parser<Info> = seq(readBytesToInt2(),
        { seq(readBytesToInt2(), { seq(readBytesToInt2(),
            { flatMap(readBytesToInt2(), { n -> attributes(n) } ) },
            {desc, list -> Pair(desc, list) }
        ) }, {name, p -> Pair(name, p)} ) }, {flag, pp -> Info(flag, pp.first, pp.second.first, pp.second.second)}
    )

    private fun infos(n: Int): Parser<List<Info>> = seqn(n, { info() }, emptyList(), { list, i -> list + i })

    private fun attributes(n: Int): Parser<List<Attribute>> = seqn(n, { attribute() }, emptyList(), { list, i -> list + i })

    fun classFile(): Parser<ClassFile> = seq(magic(), { seq(version(), { seq(flatMap(count(), { n -> constantPool(n) }),
        { seq(access_flags(), { seq(this_class(), { seq(super_class(), { seq(flatMap(count(), {n -> interfaces(n)}),
        { seq(flatMap(count(), {n -> infos(n)}), { seq(flatMap(count(), {n -> infos(n)}),
        { flatMap(count(), {n -> attributes(n)}) },
        {m, atr -> Pair(listOf(m) ,atr)}) },
        {f, p -> Pair(listOf(f) + p.first, p.second) }) },
        {int, p -> Pair(int, p)}) },
        { sc, p -> Pair(listOf(sc), p)}) },
        { tc, p -> Pair(p.first + tc, p.second)}) },
        { af, p -> Pair(p.first + af, p.second)}) },
        { cp, p -> Pair(cp, p)}) },
        { v, p -> ClassFile(v, p.first, p.second.first[2],  p.second.first[1],  p.second.first[0],
        p.second.second.first, p.second.second.second.first[0], p.second.second.second.first[1],
            p.second.second.second.second)}) },
        { _, cl -> cl })
}
