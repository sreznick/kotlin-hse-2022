package bytecode

data class ConstantPool(
    var constantClassIndex: MutableMap<Int, Int> = mutableMapOf(),
    var fieldRef: Pair<Int, Int>? = null,
    var methodRef: Pair<Int, Int>? = null,
    var interfaceMethodRef: Pair<Int, Int>? = null,
    var stringIndex: MutableList<Int> = mutableListOf(),
    var integer: MutableList<Int> = mutableListOf(),
    var float: MutableList<Float> = mutableListOf(),
    var long: MutableList<Long> = mutableListOf(),
    var double: MutableList<Double> = mutableListOf(),
    var nameAndType: Pair<Int, Int>? = null,
    var utf_8: MutableMap<Int, String> = mutableMapOf(),
    var methodHandle: Pair<Int, Int>? = null,
    var methodType: Int? = null,
    var dynamic: Pair<Int, Int>? = null,
    var invokeDynamic: Pair<Int, Int>? = null,
    var module: Int? = null,
    var constantPackage: Int? = null
)
