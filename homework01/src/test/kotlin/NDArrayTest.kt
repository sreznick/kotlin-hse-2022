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
            for (j in 0 until 3) {
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
            for (j in 0 until 3) {
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
    fun testAdd1() {
        val data = DefaultNDArray.ones(DefaultShape(5, 3))
        val data1 = DefaultNDArray.ones(DefaultShape(5))
        data1.set(DefaultPoint(0), 3)
        data1.set(DefaultPoint(4), 3)

        data.add(data1)

        assertEquals(4, data.at(DefaultPoint(0, 0)))
        assertEquals(4, data.at(DefaultPoint(0, 1)))
        assertEquals(4, data.at(DefaultPoint(0, 2)))

        assertEquals(2, data.at(DefaultPoint(1, 0)))
        assertEquals(2, data.at(DefaultPoint(1, 1)))
        assertEquals(2, data.at(DefaultPoint(1, 2)))

        assertEquals(4, data.at(DefaultPoint(4, 0)))
        assertEquals(4, data.at(DefaultPoint(4, 1)))
        assertEquals(4, data.at(DefaultPoint(4, 2)))
    }

    @Test
    fun testMul() {
        val data = DefaultNDArray.ones(DefaultShape(2, 3))
        val data1 = DefaultNDArray.ones(DefaultShape(3))
        data.set(DefaultPoint(0, 0), 1)
        data.set(DefaultPoint(0, 1), -1)
        data.set(DefaultPoint(0, 2), 2)
        data.set(DefaultPoint(1, 0), 0)
        data.set(DefaultPoint(1, 1), -3)
        data.set(DefaultPoint(1, 2), 1)

        data1.set(DefaultPoint(0), 2)
        data1.set(DefaultPoint(1), 1)
        data1.set(DefaultPoint(2), 0)


        val data3 = data.dot(data1)

        assertEquals(1, data3.at(DefaultPoint(0)))
        assertEquals(-3, data3.at(DefaultPoint(1)))
    }

    @Test
    fun testMul1() {
        val data = DefaultNDArray.ones(DefaultShape(2, 3))
        val data1 = DefaultNDArray.ones(DefaultShape(3, 2))
        data.set(DefaultPoint(0, 0), 0)
        data.set(DefaultPoint(0, 1), 4)
        data.set(DefaultPoint(0, 2), -2)
        data.set(DefaultPoint(1, 0), -4)
        data.set(DefaultPoint(1, 1), -3)
        data.set(DefaultPoint(1, 2), 0)

        data1.set(DefaultPoint(0, 0), 0)
        data1.set(DefaultPoint(0, 1), 1)
        data1.set(DefaultPoint(1, 0), 1)
        data1.set(DefaultPoint(1, 1), -1)
        data1.set(DefaultPoint(2, 0), 2)
        data1.set(DefaultPoint(2, 1), 3)


        val data3 = data.dot(data1)

        assertEquals(0, data3.at(DefaultPoint(0, 0)))
        assertEquals(-10, data3.at(DefaultPoint(0, 1)))
        assertEquals(-3, data3.at(DefaultPoint(1, 0)))
        assertEquals(-1, data3.at(DefaultPoint(1, 1)))
    }
}