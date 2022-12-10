package homework03

import homework03.csv.csvSerialize
import org.junit.Test
import kotlin.test.assertEquals

class CsvTest {
    @Test
    fun csvSerialize_numberTypesNonFloating() {
        val expected = """
            "value"
            4
            3
            
        """.trimIndent()
        println(expected)
        assertEquals(csvSerialize(listOf(4, 3), Int::class), expected)
        assertEquals(csvSerialize(listOf(4, 3), Short::class), expected)
        assertEquals(csvSerialize(listOf(4L, 3L), Long::class), expected)
        assertEquals(csvSerialize(listOf(4, 3), Byte::class), expected)
    }

    @Test
    fun csvSerialize_numberTypesFloating() {
        val expected = """
            "value"
            4.5
            3.2
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf(4.5F, 3.2F), Float::class), expected)
        assertEquals(csvSerialize(listOf(4.5, 3.2), Double::class), expected)
    }

    @Test
    fun csvSerialize_boolean() {
        val expected = """
            "value"
            true
            false
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf(true, false), Boolean::class), expected)
    }

    @Test
    fun csvSerialize_char() {
        val expected = """
            "value"
            b
            a
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf('b', 'a'), Char::class), expected)
    }

    @Test
    fun csvSerialize_string() {
        val expected = """
            "value"
            "aba"
            "caba"
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf("aba", "caba"), String::class), expected)
    }

    @Test
    fun csvSerialize_customClass() {
        data class A(val x: Int, val y: String)
        val expected = """
            "x","y"
            3,"4"
            1,"aba"
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf(A(3, "4"), A(1, "aba")), A::class), expected)
    }

    @Test
    fun csvSerialize_customClassWithInnerCustomClass() {
        data class A(val x: Int, val y: String)
        data class B(val a: A, val b: Int)
        val expected = """
            "a","b"
            A(x=3, y=4),5
            A(x=1, y=aba),-10
            
        """.trimIndent()
        assertEquals(csvSerialize(listOf(B(A(3, "4"), 5), B(A(1, "aba"), -10)), B::class), expected)
    }
}