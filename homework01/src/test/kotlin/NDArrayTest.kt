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

        assertEquals(76, data.at(DefaultPoint(1, 0)))
        assertEquals(2, data.at(DefaultPoint(1, 1)))
        assertEquals(2, data.at(DefaultPoint(1, 2)))

        assertEquals(2, data.at(DefaultPoint(2, 0)))
        assertEquals(5, data.at(DefaultPoint(2, 1)))
        assertEquals(2, data.at(DefaultPoint(2, 2)))

        assertEquals(2, data.at(DefaultPoint(3, 0)))
        assertEquals(2, data.at(DefaultPoint(3, 1)))
        assertEquals(35, data.at(DefaultPoint(3, 2)))
    }

    @Test
    fun testAddDiffDims2D() {
        val data1 = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = DefaultNDArray.ones(DefaultShape(5))

        data2.set(DefaultPoint(2), 99)

        data1.add(data2)

        assertEquals(2, data1.at(DefaultPoint(1, 0)))
        assertEquals(100, data1.at(DefaultPoint(2, 0)))
        assertEquals(2, data1.at(DefaultPoint(3, 0)))

        assertEquals(2, data1.at(DefaultPoint(1, 1)))
        assertEquals(100, data1.at(DefaultPoint(2, 1)))
        assertEquals(2, data1.at(DefaultPoint(3, 1)))

        assertEquals(2, data1.at(DefaultPoint(1, 2)))
        assertEquals(100, data1.at(DefaultPoint(2, 2)))
        assertEquals(2, data1.at(DefaultPoint(3, 2)))
    }

    @Test
    fun testAddDiffDims3D() {
        val data1 = DefaultNDArray.ones(DefaultShape(10, 3, 5))
        val data2 = DefaultNDArray.ones(DefaultShape(10, 5))

        for (i in 0 until 10) {
            data2.set(DefaultPoint(i, 2), 0)
        }
        for (i in 0 until 5) {
            data2.set(DefaultPoint(4, i), 99)
        }

        data1.add(data2)

        for (i in 0 until 3) {
            for (j in 0 until 5) {
                if (j != 2) {
                    assertEquals(2, data1.at(DefaultPoint(3, i, j)))
                    assertEquals(2, data1.at(DefaultPoint(5, i, j)))
                }
                assertEquals(100, data1.at(DefaultPoint(4, i, j)))
            }
        }

        for (i in 0 until 10) {
            for (j in 0 until 3) {
                if (i != 4) {
                    assertEquals(2, data1.at(DefaultPoint(i, j, 1)))
                    assertEquals(1, data1.at(DefaultPoint(i, j, 2)))
                    assertEquals(2, data1.at(DefaultPoint(i, j, 3)))
                }
            }
        }
    }

    @Test
    fun testDotMatrix() {
        val data1 = DefaultNDArray.zeros(DefaultShape(2, 3))
        val data2 = DefaultNDArray.zeros(DefaultShape(3, 4))

        // 5 -1 0
        // 2  1 1
        data1.set(point(0, 0), 5)
        data1.set(point(0, 1), -1)
        data1.set(point(0, 2), 0)

        data1.set(point(1, 0), 2)
        data1.set(point(1, 1), 1)
        data1.set(point(1, 2), 1)


        //0 3 -1 2
        //1 -2 4 1
        //8 -3 6 7
        data2.set(point(0, 0), 0)
        data2.set(point(0, 1), 3)
        data2.set(point(0, 2), -1)
        data2.set(point(0, 3), 2)

        data2.set(point(1, 0), 1)
        data2.set(point(1, 1), -2)
        data2.set(point(1, 2), 4)
        data2.set(point(1, 3), 1)

        data2.set(point(2, 0), 8)
        data2.set(point(2, 1), -3)
        data2.set(point(2, 2), 6)
        data2.set(point(2, 3), 7)

        val res = data1.dot(data2)

        //-1 17 -9 9
        //9 1 8 12
        assertEquals(-1, res.at(point(0, 0)))
        assertEquals(17, res.at(point(0, 1)))
        assertEquals(-9, res.at(point(0, 2)))
        assertEquals(9, res.at(point(0, 3)))

        assertEquals(9, res.at(point(1, 0)))
        assertEquals(1, res.at(point(1, 1)))
        assertEquals(8, res.at(point(1, 2)))
        assertEquals(12, res.at(point(1, 3)))
    }

    @Test
    fun testDotVector() {
        val data1 = DefaultNDArray.zeros(DefaultShape(3, 4))
        val data2 = DefaultNDArray.zeros(DefaultShape(4))

        //0 3 -1 2
        //1 -2 4 1
        //8 -3 6 7
        data1.set(point(0, 0), 0)
        data1.set(point(0, 1), 3)
        data1.set(point(0, 2), -1)
        data1.set(point(0, 3), 2)

        data1.set(point(1, 0), 1)
        data1.set(point(1, 1), -2)
        data1.set(point(1, 2), 4)
        data1.set(point(1, 3), 1)

        data1.set(point(2, 0), 8)
        data1.set(point(2, 1), -3)
        data1.set(point(2, 2), 6)
        data1.set(point(2, 3), 7)

        //1
        //9
        //4
        //8
        data2.set(point(0), 1)
        data2.set(point(1), 9)
        data2.set(point(2), 4)
        data2.set(point(3), 8)

        val res = data1.dot(data2)

        assertEquals(39, res.at(point(0)))
        assertEquals(7, res.at(point(1)))
        assertEquals(61, res.at(point(2)))
    }

    private fun point(vararg coordinates: Int): DefaultPoint {
        return DefaultPoint(*coordinates)
    }
}