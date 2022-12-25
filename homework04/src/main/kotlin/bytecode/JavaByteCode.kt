package bytecode

data class JavaByteCode(
    var minorVersion: Int = 0,
    var majorVersion: Int = 0,
    var constantPool: ConstantPool? = null,
    var accessFlag: Int = 0,
    var thisClass: Int = 0,
    var superClass: Int = 0,
    var interfaces: List<Int> = listOf(),
    var fieldInfo: List<Info> = listOf(),
    var methodInfo: List<Info> = listOf(),
    var classAttributesInfo: List<Int> = listOf()
) {

    private fun convertAccessFlag(accessFlag: Int, map: Map<Int, String>, s1: String = "", s2: String = "") =
        buildString {
            var cur = accessFlag
            val flags = ArrayDeque(map.keys)
            while (cur > 0) {
                val head = flags.removeLast()
                if (cur >= head) {
                    cur -= head
                    append(s1)
                    append("${map[head]}")
                    append(s2)
                }
            }
        }


    private fun convertClassAccessFlag(): String {
        val accessFlagsTable = mapOf(
            0x0001 to "public",
            0x0010 to "final",
            0x0020 to "super",
            0x0200 to "interface",
            0x0400 to "abstract",
            0x1000 to "synthetic",
            0x2000 to "annotation",
            0x4000 to "enum",
            0x8000 to "module"
        )
        return convertAccessFlag(accessFlag, accessFlagsTable, "\t", "\n")
    }

    private fun convertFieldAccessFlag(flag: Int): String {
        val accessFlagsTable = mapOf(
            0x0001 to "public",
            0x0002 to "private",
            0x0004 to "protected",
            0x0008 to "static",
            0x0010 to "final",
            0x0040 to "volatile",
            0x0080 to "transient",
            0x1000 to "synthetic",
            0x4000 to "ENUM"

        )

        return convertAccessFlag(flag, accessFlagsTable, ", ")
    }

    private fun convertMethodAccessFlag(flag: Int): String {
        val accessFlagsTable = mapOf(
            0x0001 to "public",
            0x0002 to "private",
            0x0004 to "protected",
            0x0008 to "static",
            0x0010 to "final",
            0x0020 to "synchronized",
            0x0040 to "bridge",
            0x0080 to "varargs",
            0x0100 to "native",
            0x0400 to "abstract",
            0x0800 to "strict",
            0x1000 to "synthetic"
        )

        return convertAccessFlag(flag, accessFlagsTable, ", ")
    }

    override fun toString(): String =
        buildString {
            append("Minor version: $minorVersion\n")
            append("Major version: $majorVersion\n")
            append("Access flags:\n")
            append(convertClassAccessFlag())
            append("Class name: ${constantPool!!.utf_8[constantPool!!.constantClassIndex[thisClass]]}\n")
            append("Superclass name: ${constantPool!!.utf_8[constantPool!!.constantClassIndex[superClass]]}\n")
            append("Interfaces:\n")
            for (interfaceNameIndex in interfaces) {
                append("\t${constantPool!!.utf_8[constantPool!!.constantClassIndex[interfaceNameIndex]]}\n")
            }
            append("Fields:\n")
            for (field in fieldInfo) {
                append("\t${constantPool!!.utf_8[field.nameIndex]}: ${constantPool!!.utf_8[field.descriptorIndex]}${convertFieldAccessFlag(field.accessFlags)}\n")
            }
            append("Methods:\n")
            for (method in methodInfo) {
                append("\t${constantPool!!.utf_8[method.nameIndex]}: ${constantPool!!.utf_8[method.descriptorIndex]}${convertMethodAccessFlag(method.accessFlags)}\n")
            }
            append("Class attributes names:\n")
            for (attr in classAttributesInfo) {
                append("\t${constantPool!!.utf_8[attr]}\n")
            }
        }
}