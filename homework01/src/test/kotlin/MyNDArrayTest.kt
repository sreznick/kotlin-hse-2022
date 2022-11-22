import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class MyNDArrayTest {
    @Test
    fun testAt() {
        val a = DefaultNDArray.ones(DefaultShape(4, 1, 6))
        org.junit.jupiter.api.assertThrows<Throwable> { a.at(DefaultPoint(-1, 0, 0)) }
        org.junit.jupiter.api.assertThrows<Throwable> { a.at(DefaultPoint(0, 0)) }
        org.junit.jupiter.api.assertThrows<Throwable> { a.at(DefaultPoint(0, 1, 8)) }
        org.junit.jupiter.api.assertThrows<Throwable> { a.at(DefaultPoint(0, 1, 0)) }
        org.junit.jupiter.api.assertThrows<Throwable> { a.at(DefaultPoint(0, 0, 0, 0)) }
        org.junit.jupiter.api.assertDoesNotThrow { a.at(DefaultPoint(3, 0, 5)) }
    }

    @Test
    fun testMicroAdd() {
        val a = DefaultNDArray.zeroes(DefaultShape(2, 3))
        val b = DefaultNDArray.ones(DefaultShape(2))
        b.set(DefaultPoint(0), 12)
        a.add(b)
        println(a)
    }

    @Test
    fun testSmallAdd() {
        val a = DefaultNDArray.zeroes(DefaultShape(3, 2, 2))
        val b = DefaultNDArray.ones(DefaultShape(3, 2))
        b.set(DefaultPoint(0, 1), 13)
        b.set(DefaultPoint(1, 0), 200)
        a.add(b)
        println(a)
        for (k in 0 until 2)
            for (i in 0 until 3)
                for (j in 0 until 2) {
                    var need = if (i == 0 && j == 1) 13 else 1
                    if (i == 1 && j == 0) need = 200
                    assertEquals(need, a.at(DefaultPoint(i, j, k)))
                }
    }

    @Test
    fun testAdd() {
        val a = DefaultNDArray.ones(DefaultShape(5, 7, 8, 2))
        val b = DefaultNDArray.ones(DefaultShape(5, 7, 8))
        b.set(DefaultPoint(4, 3, 6), -100)
        b.set(DefaultPoint(1, 6, 2), 200)
        a.add(b)
        for (k in 0 until 2)
            for (i in 0 until 5)
                for (j in 0 until 7)
                    for (l in 0 until 8) {
                        var need = if (i == 4 && j == 3 && l == 6) -99 else 2
                        if (i == 1 && j == 6 && l == 2) need = 201
                        assertEquals(need, a.at(DefaultPoint(i, j, l, k)))
                    }
    }

    @Test
    fun testAddThrow() {
        val a = DefaultNDArray.ones(DefaultShape(3, 5, 4))
        val b = DefaultNDArray.ones(DefaultShape(3, 5, 4, 9))
        org.junit.jupiter.api.assertThrows<Throwable> { a.add(b) }
    }

    @Test
    fun testView() {
        val a = DefaultNDArray.zeroes(DefaultShape(2, 3))
        val b = a.view()
        a.set(DefaultPoint(1, 0), -9)
        b.set(DefaultPoint(0, 1), 10)
        for (i in 0 until 2)
            for (j in 0 until 3) {
                assertEquals(
                    a.at(DefaultPoint(i, j)), when {
                        i == 1 && j == 0 -> -9
                        i == 0 && j == 1 -> 10
                        else -> 0
                    }
                )
                assertEquals(
                    b.at(DefaultPoint(i, j)), when {
                        i == 1 && j == 0 -> -9
                        i == 0 && j == 1 -> 10
                        else -> 0
                    }
                )
            }
    }

    @Test
    fun testView2() {
        var a: NDArray? = DefaultNDArray.zeroes(DefaultShape(2, 3))
        val b = a?.view()
        a?.set(DefaultPoint(1, 0), -9)
        b?.set(DefaultPoint(0, 1), 10)
        for (i in 0 until 2)
            for (j in 0 until 3) {
                assertEquals(
                    b?.at(DefaultPoint(i, j)), when {
                        i == 1 && j == 0 -> -9
                        i == 0 && j == 1 -> 10
                        else -> 0
                    }
                )
                assertEquals(
                    a?.at(DefaultPoint(i, j)), when {
                        i == 1 && j == 0 -> -9
                        i == 0 && j == 1 -> 10
                        else -> 0
                    }
                )
            }
        a = null
        for (i in 0 until 2)
            for (j in 0 until 3)
                assertEquals(b?.at(DefaultPoint(i, j)), when {
                    i == 1 && j == 0 -> -9
                    i == 0 && j == 1 -> 10
                    else -> 0
                })
        assert(Objects.equals(null, a))
    }

    @Test
    fun testView3() {
        val a = DefaultNDArray.ones(DefaultShape(3, 5, 6, 10))
        val b = a.view()
        a.set(DefaultPoint(2, 1, 3, 8), 100)
        val c = b.view()
        c.set(DefaultPoint(0, 0, 5, 1), -90)
        assertEquals(a.at(DefaultPoint(2, 1, 3, 8)), 100)
        assertEquals(b.at(DefaultPoint(2, 1, 3, 8)), 100)
        assertEquals(c.at(DefaultPoint(2, 1, 3, 8)), 100)
        assertEquals(a.at(DefaultPoint(0, 0, 5, 1)), -90)
        assertEquals(b.at(DefaultPoint(0, 0, 5, 1)), -90)
        assertEquals(c.at(DefaultPoint(0, 0, 5, 1)), -90)
    }

    private fun matrixToNDArray(a: Array<IntArray>): NDArray {
        val ans = DefaultNDArray.zeroes(DefaultShape(a.size, a[0].size))
        for (i in 0 until ans.dim(0))
            for (j in 0 until ans.dim(1))
                ans.set(DefaultPoint(i, j), a[i][j])
        return ans
    }

    private fun arrayToNDArray(n: Int, vararg a: Int): NDArray {
        val m = a.size / n
        val ans = DefaultNDArray.zeroes(DefaultShape(n, m))
        for (i in 0 until n)
            for (j in 0 until m)
                ans.set(DefaultPoint(i, j), a[i * m + j])
        return ans
    }

    private fun assertEqualsMatrix(a: NDArray, b: IntArray) {
        assertEquals(a.ndim, 2)
        val n = a.dim(0)
        val m = a.dim(1)
        assertEquals(n * m, b.size)
        for (i in 0 until n)
            for (j in 0 until m)
                assertEquals(a.at(DefaultPoint(i, j)), b[i * m + j])
    }

    private fun assertEqualsMatrix(a: NDArray, b: NDArray) {
        assertEquals(a.ndim, 2)
        assertEquals(b.ndim, 2)
        val n = a.dim(0)
        val m = a.dim(1)
        assertEquals(n, b.dim(0))
        assertEquals(m, b.dim(1))
        for (i in 0 until n)
            for (j in 0 until m)
                assertEquals(a.at(DefaultPoint(i, j)), b.at(DefaultPoint(i, j)))
    }

    @Test
    fun testDot() {
        val a = matrixToNDArray(arrayOf(intArrayOf(2, 5, 6), intArrayOf(7, -100, 0)))
        val b = matrixToNDArray(arrayOf(intArrayOf(-9, 6, -6, 0), intArrayOf(1, -90, 55, 5), intArrayOf(45, 69, -9, -75)))
        assertEqualsMatrix(a.dot(b), intArrayOf(257, -24, 209, -425, -163, 9042, -5542, -500))
    }

    @Test
    fun testDot2() {
        val a = arrayToNDArray(4, 58, 90, -12, 5, 541, 95, -80, 5, 10, 3, 7, -78, 0, 0, 0, 0)
        val b = DefaultNDArray.ones(DefaultShape(4))
        b.set(DefaultPoint(1), 59)
        b.set(DefaultPoint(2), 90)
        b.set(DefaultPoint(3), -75)
        val c = a.dot(b)
        assertEquals(c.at(DefaultPoint(0)), 3913)
        assertEquals(c.at(DefaultPoint(1)), -1429)
        assertEquals(c.at(DefaultPoint(2)), 6667)
        assertEquals(c.at(DefaultPoint(3)), 0)
    }

    @Test
    fun testDot3() {
        val a = arrayToNDArray(2, 1, 5622, 1, 5623)
        val aInv = arrayToNDArray(2, 5623, -5622, -1, 1)
        assertEqualsMatrix(a.dot(aInv), intArrayOf(1, 0, 0, 1))
        val b = arrayToNDArray(2, 3928, 0, 0, 3928)
        assertEqualsMatrix(b, a.dot(b).dot(aInv))
    }

    @Test
    fun testDot4() {
        val a = arrayToNDArray(2, 1, 25, 0, -24)
        val b = arrayToNDArray(2, 54, 18, 0, 36)
        val zero = arrayToNDArray(2, 0, 0, 0, 0)
        assertEqualsMatrix(a.dot(b), b.dot(a))
        assertEqualsMatrix(a.dot(zero), zero)
        assertEqualsMatrix(b.dot(zero), zero)
    }

    @Test
    fun testDotThrow() {
        val a = arrayOf(DefaultNDArray.ones(DefaultShape(1, 5, 8))
            , DefaultNDArray.ones(DefaultShape(5, 8))
            , DefaultNDArray.ones(DefaultShape(5, 9))
            , DefaultNDArray.ones(DefaultShape(5)))
        a.forEach { x -> a.forEach { org.junit.jupiter.api.assertThrows<Throwable> { x.dot(it) } } }
    }
}
