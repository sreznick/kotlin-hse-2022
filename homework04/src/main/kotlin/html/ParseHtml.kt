package html

import Left
import Right
import java.io.*

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Some arguments are missing")
        return
    }
    val input = args[0]
    val output = args[1]
    val parsers = HtmlParser()
    try {
        val inputFile = File(input).bufferedReader()
        val text = inputFile.readText()
        inputFile.close()
        val outputFile = File(output).printWriter()
        outputFile.use {
            when (val result = parsers.run(parsers.parseHtml(), text)) {
                is Right -> outputFile.println(result.value.a)
                is Left -> outputFile.println("${result.value.get.stack[0].first}, ${result.value.get.stack[0].second}")
            }
        }
    } catch (e: FileNotFoundException) {
        println("Can't find file: $input")
    } catch (e: IOException) {
        println("Something went wrong while working with given files")
    }
}