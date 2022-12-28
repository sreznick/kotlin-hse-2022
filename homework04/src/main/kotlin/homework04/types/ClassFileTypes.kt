package homework04.types

sealed class Constant
data class ClassInfoConstant(val nameIndex: Int) : Constant() // tag = 7
data class UTFConstant(val name: String) : Constant() // tag = 1
data class OtherConstant(val tag: Int) : Constant()

data class FieldOrMethodParsed(
    val accessFlags: List<AccessFlags<*>>,
    val nameIndex: Int,
    val descriptorIndex: Int,
    val attributesNamesIndexes: List<Int>
)

interface AccessFlags<T : Enum<T>> {
    val code: Int
}

enum class ClassAccessFlags(override val code: Int) : AccessFlags<ClassAccessFlags> {
    Public(0x0001),
    Final(0x0010),
    Super(0x0020),
    Interface(0x0200),
    Abstract(0x0400),
    Synthetic(0x1000),
    Annotation(0x2000),
    Enum(0x4000),
    Module(0x8000)
}

enum class FieldAccessFlags(override val code: Int) : AccessFlags<FieldAccessFlags> {
    Public(0x0001),
    Private(0x0002),
    Protected(0x0004),
    Static(0x0008),
    Final(0x0010),
    Volatile(0x0040),
    Transient(0x0080),
    Synthetic(0x1000),
    Enum(0x4000)
}

enum class MethodAccessFlags(override val code: Int) : AccessFlags<MethodAccessFlags> {
    Public(0x0001),
    Private(0x0002),
    Protected(0x0004),
    Static(0x0008),
    Final(0x0010),
    Synchronized(0x0020),
    Bridge(0x0040),
    Varargs(0x0080),
    Native(0x0100),
    Abstract(0x0400),
    Strict(0x0800),
    Synthetic(0x1000)
}

data class Field(
    val name: String,
    val type: String,
    val accessFlags: List<FieldAccessFlags>,
    val attributes: List<String>
)

data class Method(
    val name: String,
    val type: String,
    val accessFlags: List<MethodAccessFlags>,
    val attributes: List<String>
)

data class ClassFile(
    val version: String,
    val accessFlags: List<ClassAccessFlags>,
    val className: String,
    val superName: String,
    val interfaces: List<String>,
    val fields: List<Field>,
    val methods: List<Method>,
    val attributes: List<String>
)