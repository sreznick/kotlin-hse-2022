import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class NDArrayTest {
    @Test
    fun testZeros() {
        val data = DefaultNDArray.zeros(DefaultShape(10))

        for (i in 0 until 10) {
            assertEquals(0, data.at(DefaultPoint(i)))
        }
    }

    @Test
    fun testOnes() {
        val data = DefaultNDArray.ones(DefaultShape(10))

        for (i in 0 until 10) {
            assertEquals(1, data.at(DefaultPoint(i)))
        }
    }

    @Test
    fun testSet1D() {
        val data = DefaultNDArray.ones(DefaultShape(10))
        data.set(DefaultPoint(3), 34)

        for (i in 0 until 10) {
            if (i != 3) {
                assertEquals(1, data.at(DefaultPoint(i)))
            } else {
                assertEquals(34, data.at(DefaultPoint(i)))
            }
        }
    }

    @Test
    fun testSet2D() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5))
        data.set(DefaultPoint(3, 4), 34)

        for (i in 0 until 10) {
            for (j in 0 until 5) {
                if (i == 3 && j == 4) {
                    assertEquals(34, data.at(DefaultPoint(i, j)))
                } else {
                    assertEquals(1, data.at(DefaultPoint(i, j)))
                }
            }
        }
    }

    @Test
    fun testSet3D() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5, 8))
        data.set(DefaultPoint(3, 4, 6), 34)

        for (i in 0 until 10) {
            for (j in 0 until 5) {
                for (k in 0 until 8) {
                    if (i == 3 && j == 4 && k == 6) {
                        assertEquals(34, data.at(DefaultPoint(i, j, k)))
                    } else {
                        assertEquals(1, data.at(DefaultPoint(i, j, k)))
                    }
                }
            }
        }
    }

    @Test
    fun testCopy() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5))
        val data2 = data.copy()

        data.set(DefaultPoint(3, 4), 34)
        data2.set(DefaultPoint(4, 3), 34)

        assertEquals(34, data.at(DefaultPoint(3, 4)))
        assertEquals(1, data.at(DefaultPoint(4, 3)))
        assertEquals(1, data2.at(DefaultPoint(3, 4)))
        assertEquals(34, data2.at(DefaultPoint(4, 3)))
    }

    @Test
    fun testAdd() {
        val data = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = data.copy()

        data.set(DefaultPoint(3, 2), 34)
        data2.set(DefaultPoint(2, 1), 4)
        data2.set(DefaultPoint(1, 0), 75)
        data.set(DefaultPoint(0, 1), 57)

        data.add(data2)

        assertEquals(2, data.at(DefaultPoint(0, 0)))
        assertEquals(58, data.at(DefaultPoint(0, 1)))
        assertEquals(2, data.at(DefaultPoint(0, 2)))
    }

    @Test
    fun testView() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5))
        val data2 = data.view()
        val data3 = data2.view().view().view().view()
        data.set(DefaultPoint(3, 4), 34)
        data2.set(DefaultPoint(4, 3), 34)

        assertEquals(34, data.at(DefaultPoint(3, 4)))
        assertEquals(34, data.at(DefaultPoint(4, 3)))
        assertEquals(34, data2.at(DefaultPoint(3, 4)))
        assertEquals(34, data2.at(DefaultPoint(4, 3)))
        assertEquals(34, data3.at(DefaultPoint(3, 4)))
        assertEquals(34, data3.at(DefaultPoint(4, 3)))
    }

    @Test
    fun testAddWithDifferentShape() {
        val data = DefaultNDArray.ones(DefaultShape(3, 2, 2))
        val data2 = DefaultNDArray.ones(DefaultShape(3, 2))
        data2.add(data2)
        data2.set(DefaultPoint(2, 1), 10)

        data.add(data2)

        assertEquals(11, data.at(DefaultPoint(2, 1, 0)))
        assertEquals(11, data.at(DefaultPoint(2, 1, 1)))
        assertEquals(3, data.at(DefaultPoint(2, 0, 1)))
    }

    @Test
    fun testVectorDotVector() {
        val v1 = DefaultNDArray.ones(DefaultShape(1, 5))
        val v2 = DefaultNDArray.ones(DefaultShape(5, 1))
        val w1 = v1.copy()
        val w2 = v2.copy()

        assertDoesNotThrow { v1.dot(v2) }
        assertDoesNotThrow { w2.dot(w1) }

        val res1 = v1.dot(v2)
        val res2 = w2.dot(w1)


        assertEquals(res2.dim(0), 5)
        assertEquals(res2.dim(1), 5)

        assertEquals(res1.at(DefaultPoint(0, 0)), 5)

        assertEquals(res2.dim(0), 5)
        assertEquals(res2.dim(1), 5)
    }

    @Test
    fun testWithAddDifferentShape() {
        val data = DefaultNDArray.ones(DefaultShape(4, 3, 2))
        val data2 = DefaultNDArray.ones(DefaultShape(4, 3))

        data.set(DefaultPoint(2, 2, 0), 34)
        data2.set(DefaultPoint(2, 2), 4)
        data2.set(DefaultPoint(0, 1), 75)
        data.set(DefaultPoint(0, 1, 1), 57)


        assertEquals(5, result.size)
        assertTrue(result.ndim in (1..2))

        if (result.ndim == 1) {
            assertTrue(result.ndim == 1)
            assertEquals(5, result.dim(0))

            assertEquals(0 * 1 + 1 * 11 + 2 * 21, result.at(DefaultPoint(0)))
            assertEquals(10 * 1 + 11 * 11 + 12 * 21, result.at(DefaultPoint(1)))
            assertEquals(20 * 1 + 21 * 11 + 22 * 21, result.at(DefaultPoint(2)))
            assertEquals(30 * 1 + 31 * 11 + 32 * 21, result.at(DefaultPoint(3)))
            assertEquals(40 * 1 + 41 * 11 + 42 * 21, result.at(DefaultPoint(4)))
        } else {
            assertTrue(result.ndim == 2)
            assertEquals(5, result.dim(0))
            assertEquals(1, result.dim(1))

            assertEquals(0 * 1 + 1 * 11 + 2 * 21, result.at(DefaultPoint(0, 0)))
            assertEquals(10 * 1 + 11 * 11 + 12 * 21, result.at(DefaultPoint(1, 0)))
            assertEquals(20 * 1 + 21 * 11 + 22 * 21, result.at(DefaultPoint(2, 0)))
            assertEquals(30 * 1 + 31 * 11 + 32 * 21, result.at(DefaultPoint(3, 0)))
            assertEquals(40 * 1 + 41 * 11 + 42 * 21, result.at(DefaultPoint(4, 0)))
        }
    }
}
