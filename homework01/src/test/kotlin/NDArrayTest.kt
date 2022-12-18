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
    fun testZeros2D() {
        val data = DefaultNDArray.zeros(DefaultShape(10, 2))

        for (i in 0 until 10) {
            for (j in 0 until 2) {
                assertEquals(0, data.at(DefaultPoint(i, j)))
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
    fun testOnes2D() {
        val data = DefaultNDArray.ones(DefaultShape(10, 2))

        for (i in 0 until 10) {
            for (j in 0 until 2) {
                assertEquals(1, data.at(DefaultPoint(i, j)))
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

    @Test
    fun testView() {
        val data = DefaultNDArray.ones(DefaultShape(10, 5))
        val data2 = data.view()

        data.set(DefaultPoint(3, 4), 34)
        data2.set(DefaultPoint(4, 3), 34)

        assertEquals(34, data.at(DefaultPoint(3, 4)))
        assertEquals(34, data.at(DefaultPoint(4, 3)))
        assertEquals(34, data2.at(DefaultPoint(3, 4)))
        assertEquals(34, data2.at(DefaultPoint(4, 3)))
    }

    @Test
    fun testAdd1d() {
        val data1 = DefaultNDArray.ones(DefaultShape(5))
        val data2 = DefaultNDArray.ones(DefaultShape(5))

        data1.set(DefaultPoint(0), 12)
        data1.set(DefaultPoint(1), 23)
        data1.set(DefaultPoint(2), 34)
        data1.set(DefaultPoint(3), 45)
        data1.set(DefaultPoint(4), 56)

        data2.set(DefaultPoint(0), 2)
        data2.set(DefaultPoint(1), 4)
        data2.set(DefaultPoint(2), 6)
        data2.set(DefaultPoint(3), 8)
        data2.set(DefaultPoint(4), 10)

        data1.add(data2)

        assertEquals(14, data1.at(DefaultPoint(0)))
        assertEquals(27, data1.at(DefaultPoint(1)))
        assertEquals(40, data1.at(DefaultPoint(2)))
        assertEquals(53, data1.at(DefaultPoint(3)))
        assertEquals(66, data1.at(DefaultPoint(4)))
    }

    @Test
    fun testAdd1dSelf() {
        val data1 = DefaultNDArray.ones(DefaultShape(5))

        data1.set(DefaultPoint(0), 12)
        data1.set(DefaultPoint(1), 23)
        data1.set(DefaultPoint(2), 34)
        data1.set(DefaultPoint(3), 45)
        data1.set(DefaultPoint(4), 56)

        data1.add(data1)

        assertEquals(24, data1.at(DefaultPoint(0)))
        assertEquals(46, data1.at(DefaultPoint(1)))
        assertEquals(68, data1.at(DefaultPoint(2)))
        assertEquals(90, data1.at(DefaultPoint(3)))
        assertEquals(112, data1.at(DefaultPoint(4)))
    }


    @Test
    fun testAdd2d1d() {
        val data1 = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = DefaultNDArray.ones(DefaultShape(5))
        for (i in 0 until 5) {
            for (j in 0 until 3) {
                data1.set(DefaultPoint(i, j), i * 10 + j)
            }
        }

        data2.set(DefaultPoint(0), 12)
        data2.set(DefaultPoint(1), 23)
        data2.set(DefaultPoint(2), 34)
        data2.set(DefaultPoint(3), 45)
        data2.set(DefaultPoint(4), 56)

        data1.add(data2)

        assertEquals(12, data1.at(DefaultPoint(0, 0)))
        assertEquals(13, data1.at(DefaultPoint(0, 1)))
        assertEquals(14, data1.at(DefaultPoint(0, 2)))
        assertEquals(33, data1.at(DefaultPoint(1, 0)))
        assertEquals(34, data1.at(DefaultPoint(1, 1)))
        assertEquals(35, data1.at(DefaultPoint(1, 2)))
        assertEquals(54, data1.at(DefaultPoint(2, 0)))
        assertEquals(55, data1.at(DefaultPoint(2, 1)))
        assertEquals(56, data1.at(DefaultPoint(2, 2)))
        assertEquals(75, data1.at(DefaultPoint(3, 0)))
        assertEquals(76, data1.at(DefaultPoint(3, 1)))
        assertEquals(77, data1.at(DefaultPoint(3, 2)))
        assertEquals(96, data1.at(DefaultPoint(4, 0)))
        assertEquals(97, data1.at(DefaultPoint(4, 1)))
        assertEquals(98, data1.at(DefaultPoint(4, 2)))
    }

    @Test
    fun testDot() {
        val data1 = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = DefaultNDArray.ones(DefaultShape(3, 2))

        for (i in 0 until 5) {
            for (j in 0 until 3) {
                data1.set(DefaultPoint(i, j), i * 10 + j)
            }
        }

        for (i in 0 until 3) {
            for (j in 0 until 2) {
                data2.set(DefaultPoint(i, j), i * 10 + j)
            }
        }

        val result = data1.dot(data2)

        assertEquals(2, result.ndim)
        assertEquals(5, result.dim(0))
        assertEquals(2, result.dim(1))

        assertEquals(0 * 0 + 1 * 10 + 2 * 20, result.at(DefaultPoint(0, 0)))
        assertEquals(0 * 1 + 1 * 11 + 2 * 21, result.at(DefaultPoint(0, 1)))
        assertEquals(10 * 0 + 11 * 10 + 12 * 20, result.at(DefaultPoint(1, 0)))
        assertEquals(10 * 1 + 11 * 11 + 12 * 21, result.at(DefaultPoint(1, 1)))
        assertEquals(20 * 0 + 21 * 10 + 22 * 20, result.at(DefaultPoint(2, 0)))
        assertEquals(20 * 1 + 21 * 11 + 22 * 21, result.at(DefaultPoint(2, 1)))
        assertEquals(30 * 0 + 31 * 10 + 32 * 20, result.at(DefaultPoint(3, 0)))
        assertEquals(30 * 1 + 31 * 11 + 32 * 21, result.at(DefaultPoint(3, 1)))
        assertEquals(40 * 0 + 41 * 10 + 42 * 20, result.at(DefaultPoint(4, 0)))
        assertEquals(40 * 1 + 41 * 11 + 42 * 21, result.at(DefaultPoint(4, 1)))

        assertEquals(0, data1.at(DefaultPoint(0, 0)))
        assertEquals(1, data1.at(DefaultPoint(0, 1)))

        assertEquals(0, data2.at(DefaultPoint(0, 0)))

    }


    @Test
    fun testDotSingle() {
        val data1 = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = DefaultNDArray.ones(DefaultShape(3, 1))

        for (i in 0 until 5) {
            for (j in 0 until 3) {
                data1.set(DefaultPoint(i, j), i * 10 + j)
            }
        }

        for (i in 0 until 3) {
            data2.set(DefaultPoint(i, 0), i * 10 + 1)
        }

        val result = data1.dot(data2)

        assertTrue(result.ndim == 2)
        assertEquals(5, result.size)
        assertEquals(5, result.dim(0))

        assertEquals(0 * 1 + 1 * 11 + 2 * 21, result.at(DefaultPoint(0, 0)))
        assertEquals(10 * 1 + 11 * 11 + 12 * 21, result.at(DefaultPoint(1, 0)))
        assertEquals(20 * 1 + 21 * 11 + 22 * 21, result.at(DefaultPoint(2, 0)))
        assertEquals(30 * 1 + 31 * 11 + 32 * 21, result.at(DefaultPoint(3, 0)))
        assertEquals(40 * 1 + 41 * 11 + 42 * 21, result.at(DefaultPoint(4, 0)))
    }

    @Test
    fun testDotVector() {
        val data1 = DefaultNDArray.ones(DefaultShape(5, 3))
        val data2 = DefaultNDArray.ones(DefaultShape(3))

        for (i in 0 until 5) {
            for (j in 0 until 3) {
                data1.set(DefaultPoint(i, j), i * 10 + j)
            }
        }

        for (i in 0 until 3) {
            data2.set(DefaultPoint(i), i * 10 + 1)
        }

        val result = data1.dot(data2)

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
