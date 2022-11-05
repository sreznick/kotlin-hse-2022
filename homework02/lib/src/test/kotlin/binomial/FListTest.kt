package binomial

import kotlin.math.max
import kotlin.test.*

internal class FListTest {
    @Test
    fun testEmpty() {
        assertTrue { FList.nil.isEmpty }
        assertFalse { FList.Cons("hello", FList.nil).isEmpty }
        assertFalse { FList.Cons("hello", FList.Cons("world", FList.nil)).isEmpty }
    }

    @Test
    fun testSize() {
        assertEquals(0, FList.nil.size)
        assertEquals(1, FList.Cons("hello", FList.nil).size)
        assertEquals(2, FList.Cons("hello", FList.Cons("world", FList.nil)).size)
    }

    @Test
    fun testToList() {
        assertEquals(emptyList(), FList.nil.toList())
        assertEquals(listOf("hello"), FList.Cons("hello", FList.nil).toList())
        assertEquals(
            listOf("hello", "world"),
            FList.Cons("hello", FList.Cons("world", FList.nil)).toList()
        )
    }

    @Test
    fun testFList() {
        assertTrue { flistOf<String>().isEmpty }
        assertTrue { flistOf<Int>().isEmpty }
        assertFalse { flistOf("hello").isEmpty }
        assertFalse { flistOf("hello", "world").isEmpty }

        assertEquals(0, flistOf<String>().size)
        assertEquals(1, flistOf("hello").size)
        assertEquals(2, flistOf("hello", "world").size)
    }

    @Test
    fun testMap() {
        assertEquals(FList.nil(), FList.nil<String>().map { it.length })
        assertEquals(FList.Cons(5, FList.nil()), FList.Cons("hello", FList.nil()).map { it.length })
        assertEquals(
            FList.Cons(5, FList.Cons(6, FList.nil())),
            FList.Cons("hello", FList.Cons("people", FList.nil())).map { it.length }
        )
    }

    @Test
    fun testFilter() {
        assertEquals(FList.nil(), FList.nil<String>().filter { it.length > 0 && it[0] == 'h' })
        assertEquals(
            FList.Cons("hello", FList.nil()),
            FList.Cons("hello", FList.nil()).filter { it.length > 0 && it[0] == 'h' }
        )
        assertEquals(FList.nil(), FList.Cons("hello", FList.nil()).filter { it.length > 0 && it[0] != 'h' })
        assertEquals(
            FList.Cons("hello", FList.nil()),
            FList.Cons("hello", FList.Cons("people", FList.nil())).filter { it.length > 0 && it[0] == 'h' }
        )
        assertEquals(
            FList.Cons("people", FList.nil()),
            FList.Cons("hello", FList.Cons("people", FList.nil())).filter { it.length > 0 && it[0] != 'h' }
        )
    }

    @Test
    fun testFold() {
        val lengthSum = { acc: Int, current: String ->
            current.length + acc
        }
        assertEquals(1, flistOf<String>().fold(1, lengthSum))
        assertEquals(5, flistOf("hello").fold(0, lengthSum))
        assertEquals(11, flistOf("hello", "people").fold(0, lengthSum))
    }

    @Test
    fun testReverse() {
        assertEquals(flistOf(), flistOf<String>().reverse())
        assertEquals(flistOf("hello"), flistOf("hello").reverse())
        assertEquals(flistOf("people", "hello"), flistOf("hello", "people").reverse())
    }

    @Test
    fun testFoldFirst() {
        assertEquals(7, flistOf(1, -5, 7).foldFirst(::max))
        assertEquals(-40, flistOf(2, -4, 5).foldFirst { x, y -> x * y })

        assertEquals(flistOf(3).foldFirst(::max), 3)
        assertFails { flistOf<Int>().foldFirst(::max) }
    }

    @Test
    fun testConcat() {
        val l0 = flistOf<Int>()
        val l1 = flistOf(1, 10, 2)
        val l2 = flistOf(-5)

        assertEquals(l0 + l0, flistOf())

        assertEquals(l1, l1 + l0)
        assertEquals(l1, l0 + l1)
        assertEquals(l2, l2 + l0)
        assertEquals(l2, l0 + l2)

        assertEquals(flistOf(1, 10, 2, -5), l1 + l2)
        assertEquals(flistOf(-5, 1, 10, 2), l2 + l1)

        assertEquals(flistOf(1, 10, 2, 1, 10, 2), l1 + l1)
        assertEquals(flistOf(-5, -5), l2 + l2)
    }

    @Test
    fun testImmutable() {
        val l1 = flistOf(1, 2)
        val l2 = flistOf(3, 4)
        val l12 = l1 + l2

        assertEquals(flistOf(1, 2, 3, 4), l12)

        val l3 = l2 + flistOf(5)
        val l4 = l1 + flistOf(6)

        assertEquals(flistOf(1, 2), l1)
        assertEquals(flistOf(3, 4), l2)
        assertEquals(flistOf(1, 2, 3, 4), l12)
        assertEquals(flistOf(3, 4, 5), l3)
        assertEquals(flistOf(1, 2, 6), l4)
    }

    @Test
    fun testGet() {
        val l0 = flistOf<Int>()
        val l1 = flistOf(0, 1, 2, 3, 4, 5)

        assertFails { l0[0] }
        assertFails { l0[5] }
        assertFails { l0[-3] }

        (0..5).forEach { assertEquals(it, l1[it]) }
        assertFails { l1[-2] }
        assertFails { l1[6] }
    }
}
