import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

fun main(args: Array<String>) {
    if (args.size != 1){
        return println("Expected file name in args")
    }

    val filename= args[0]
    try {
        val uByteArray = File(filename).readBytes().toUByteArray()
        val parser = ExtendedParsers().parseJVM()
        when (val result = parser(ByteSource(uByteArray))){
            is Left->{
                println("Error while parsing jvm bytecode "+result.value.stack)
            }
            is Right->{
                println("""
                minor version: ${result.value.minorVersion}
                major version: ${result.value.majorVersion}
                class modificators: ${result.value.accessFlags} 
                class name: ${result.value.mainClassName}
                super class name: ${result.value.superClassName}
                interfaces: ${result.value.interfacesHuman}
                fields: 
            """.trimIndent())
                result.value.fieldsHuman?.forEach { println("   name: ${it.name}, descriptor: ${it.descriptor}, access flags: ${it.accessFlags}, attributes: ${it.attributes}") }
                println("methods:")
                result.value.methodsHuman?.forEach { println("  name: ${it.name}, descriptor: ${it.descriptor}, access flags: ${it.accessFlags}, attributes: ${it.attributes}") }
                println("Global attributes: ${result.value.attributesHuman}")

            }
        }
    } catch (e: FileNotFoundException){
        println("File not found")
    } catch (e: IOException){
        println("Error while working with file")
    }

}