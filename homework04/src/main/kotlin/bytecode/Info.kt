package bytecode

data class Info(
    var accessFlags: Int = 0,
    var nameIndex: Int = 0,
    var descriptorIndex: Int = 0,
    var attributesNames: List<Int> = listOf()
)
