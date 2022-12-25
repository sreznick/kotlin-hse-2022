import java.io.File

class JVMApp(private val parser: JVMByteCodeParser = JVMByteCodeParser()) {
    fun parseJVM(fileIn: String, fileOut: String) {
        when (val parsedClassFile: Result<ClassFile> =
            parser.run(parser.classFile(), File(fileIn).readText(Charsets.ISO_8859_1))) {
            is Success -> File(fileOut).writeText(parsedClassFile.value.toString())
            is Failure -> {
                println("Something went wrong:")
                println(parsedClassFile.get.stack)
            }
        }
    }
}

fun main(args: Array<String>) {
    val app = JVMApp()
    app.parseJVM(args[0], args[1])
}
