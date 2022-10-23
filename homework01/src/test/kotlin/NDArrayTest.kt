import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class NDArrayTest {
    @Test
    fun testZeros() {
        val data = DefaultNDArray.zeros(DefaultShape(10))

        for (i in 0 until 10) {
            assertEquals(0, data.at(DefaultPoint( i)))
        }
    }

    @Test
    fun testOnes() {
        val data =DefaultNDArray.ones(DefaultShape(10))

        for (i in 0 until 10) {
            assertEquals(1, data.at(DefaultPoint(i)))
        }
    }

    @Test
    fun testSet1D() {
        val data =DefaultNDArray.ones(DefaultShape(10))
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

    /* My tests */
    @Test
    fun testAdd3D() {
        val data = DefaultNDArray.zeros(DefaultShape(5, 3, 2))
        val data2 = data.copy()

        data.set(DefaultPoint(0, 0, 0), 10)
        data2.set(DefaultPoint(4, 2, 1), 4)
        data2.set(DefaultPoint(1, 1, 1), 2)
        data.set(DefaultPoint(1, 1, 1), 3)

        data.add(data2)

        assertEquals(5, data.at(DefaultPoint(1, 1, 1)))
        assertEquals(10, data.at(DefaultPoint(0, 0, 0)))
        assertEquals(4, data.at(DefaultPoint(4, 2, 1)))

    }

    @Test
    fun testAddLess() {
        val data = DefaultNDArray.zeros(DefaultShape(2, 3, 5))
        val data2 =  DefaultNDArray.zeros(DefaultShape(2, 3))

        data2.set(DefaultPoint(1, 2), 34)
        data2.set(DefaultPoint(0, 1), 4)

        data.add(data2)

        assertEquals(34, data.at(DefaultPoint(1, 2, 0)))
        assertEquals(34, data.at(DefaultPoint(1, 2, 4)))
        assertEquals(4, data.at(DefaultPoint(0, 1, 2)))

    }

    @Test
    fun testAddLess2() {
        val data = DefaultNDArray.zeros(DefaultShape(2, 3, 5, 10))
        val data2 =  DefaultNDArray.zeros(DefaultShape(2, 3, 5))

        data2.set(DefaultPoint(1, 2, 1), 34)
        data2.set(DefaultPoint(0, 1, 0), 4)

        data.add(data2)

        assertEquals(34, data.at(DefaultPoint(1, 2, 1,  0)))
        assertEquals(34, data.at(DefaultPoint(1, 2, 1,  4)))
        assertEquals(4, data.at(DefaultPoint(0, 1, 0, 9)))

    }

    @Test
    fun tesDot2D2D() {
        val data = DefaultNDArray.zeros(DefaultShape(5, 3))
        val data2 = DefaultNDArray.zeros(DefaultShape(3, 6))


        data.set(DefaultPoint(0, 0), 1)
        data.set(DefaultPoint(2, 1), 2)
        data.set(DefaultPoint(3, 0), -1)
        data.set(DefaultPoint(4, 2), 4)
        data2.set(DefaultPoint(0, 0), 1)
        data2.set(DefaultPoint(1, 3), 2)
        data2.set(DefaultPoint(2, 5), 4)

        val res = data.dot(data2)

        assertEquals(1, res.at(DefaultPoint(0, 0)))
        assertEquals(-1, res.at(DefaultPoint(3, 0)))
        assertEquals(4, res.at(DefaultPoint(2, 3)))
        assertEquals(16, res.at(DefaultPoint(4, 5)))

    }

    @Test
    fun tesDot2D1D() {
        val data = DefaultNDArray.zeros(DefaultShape(5, 3))
        val data2 = DefaultNDArray.zeros(DefaultShape(3))


        data.set(DefaultPoint(0, 0), 1)
        data.set(DefaultPoint(2, 1), 2)
        data.set(DefaultPoint(3, 0), -1)
        data.set(DefaultPoint(4, 2), 4)
        data2.set(DefaultPoint(0), 1)
        data2.set(DefaultPoint(2), 3)

        val res = data.dot(data2)

        assertEquals(1, res.at(DefaultPoint(0, 0)))
        assertEquals(-1, res.at(DefaultPoint(3, 0)))
        assertEquals(12, res.at(DefaultPoint(4, 0)))
    }

    @Test
    fun testView() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5))
        val data2 = data.view()

        data.set(DefaultPoint(3, 4), 34)
        val data3 = data2.view()
        data3.set(DefaultPoint(4, 3), 5)

        assertEquals(34, data.at(DefaultPoint(3, 4)))
        assertEquals(5, data.at(DefaultPoint(4, 3)))
        assertEquals(34, data2.at(DefaultPoint(3, 4)))
        assertEquals(5, data2.at(DefaultPoint(4, 3)))
        assertEquals(34, data3.at(DefaultPoint(3, 4)))
        assertEquals(5, data3.at(DefaultPoint(4, 3)))
    }
}