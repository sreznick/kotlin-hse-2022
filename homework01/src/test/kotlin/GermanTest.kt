import DefaultNDArray.ArrayProducer.ones
import DefaultNDArray.ArrayProducer.zeros
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.ClassCastException
import kotlin.test.assertContentEquals

internal class GermanTest {

    /*
     *
     * Point tests
     *
     */

    @Test
    fun pointCreation() {
        samplePoints()
    }

    @Test
    fun pointNdim() {
        val points = samplePoints()
        assertContentEquals(points.map { it.dimNumber }, listOf(0, 1, 5, 1000))
    }

    @Test
    fun pointDim() {
        val points = samplePoints()
        assertEquals(points[1].dim(0), 5)
        for (d in 0..4) assertEquals(-d - 1, points[2].dim(d))
        for (d in 0..999) assertEquals(1, points[3].dim(d))
    }

    /*
     *
     * Shape tests
     *
     */

    @Test
    fun emptyShape() {
        val caught = assertThrows<ShapeArgumentException.EmptyShapeException> {
            DefaultShape()
        }
        println("${caught.message}")
    }

    @Test
    fun shapeCreation() {
        sampleShapes()
    }

    @Test
    fun nonPositiveDimensionShape() {
        assertThrows<ShapeArgumentException.NonPositiveDimensionException> {
            DefaultShape(0)
        }
        assertThrows<ShapeArgumentException.NonPositiveDimensionException> {
            DefaultShape(1, 2, 0, 4, 5)
        }
        assertThrows<ShapeArgumentException.NonPositiveDimensionException> {
            DefaultShape(0, 0, 0, 0)
        }
        assertThrows<ShapeArgumentException.NonPositiveDimensionException> {
            DefaultShape(-1)
        }

        val caught = assertThrows<ShapeArgumentException.NonPositiveDimensionException> {
            DefaultShape(2, 3, 4, 5, -777)
        }
        println("${caught.message}")
    }

    @Test
    fun shapeNdim() {
        val shapes = sampleShapes()
        assertContentEquals(shapes.map { it.dimNumber }, listOf(1, 5, 3, 1000))
    }

    @Test
    fun shapeDim() {
        val shapes = sampleShapes()
        assertEquals(shapes[0].dim(0), 5)
        for (i in 0..4) assertEquals(shapes[1].dim(i), i + 1)
        assertContentEquals(arrayOf(shapes[2].dim(0), shapes[2].dim(1), shapes[2].dim(2)),
                     arrayOf(10, 11, 8)
        )
        for (i in 0..999) assertEquals(shapes[3].dim(i), 1)
    }

    @Test
    fun shapeSize() {
        val shapes = sampleShapes()
        assertContentEquals(shapes.map { it.size }, listOf(5, 120, 880, 1))
    }

    /*
     *
     * NDArray tests
     *
     */

    @Test
    fun ndArrayZeros() {
        val shapes = sampleShapes()
        val arrays = shapes.map { zeros(it) }
        for (i in arrays.indices) {
            for (j in 0 until arrays[i].size) {
                assertEquals(arrays[i].at(indexToPoint(i, shapes[i])), 0)
            }
        }
    }

    @Test
    fun ndArrayOnes() {
        val shapes = sampleShapes()
        val arrays = shapes.map { ones(it) }
        for (i in arrays.indices) {
            for (j in 0 until arrays[i].size) {
                assertEquals(arrays[i].at(indexToPoint(i, shapes[i])), 1)
            }
        }
    }

    @Test
    fun ndArrayCopy() {
        val arrays = sampleNDArrays()
        val copies = arrays.map { it.copy() }.toTypedArray()
        for (i in arrays.indices) {
            assertTrue( arrays[i].checkIdentical(copies[i]) )
            val point = indexToPoint(i, arrays[i].getShape())
            arrays[i].set(point, arrays[i].at(point) + 1)
        }
        for (i in arrays.indices) {
            assertFalse( arrays[i].checkIdentical(copies[i]) )
        }
    }

    @Test
    fun ndArrayAt() {
        val arrays = sampleNDArrays()
        assertTrue(arrays[0].at(indexToPoint(0, arrays[0].getShape())) == -1)
        assertTrue(arrays[5].at(indexToPoint(2, arrays[5].getShape())) == 3)
        assertTrue(arrays[7].at(indexToPoint(8, arrays[7].getShape())) == 2)
        assertTrue(arrays[8].at(indexToPoint(0, arrays[8].getShape())) == 1)
        assertTrue(arrays[9].at(indexToPoint(14, arrays[9].getShape())) == -7)
    }

    @Test
    fun ndArrayAtIllegalDimension() {
        val arrays = sampleNDArrays()
        val arr1 = arrays[6]
        val arr2 = arrays[8]
        val p1 = DefaultPoint(0, 0)
        val p2 = DefaultPoint(3, 0, 3)
        val p3 = DefaultPoint(2)

        assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr1.at(p1)
        }
        var caught: Throwable = assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr2.at(p2)
        }
        println(caught.message)
        caught = assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr2.at(p3)
        }
        println(caught.message)
    }

    @Test
    fun ndArrayAtIllegalCoordinate() {
        val arrays = sampleNDArrays()
        val arr1 = arrays[6]
        val arr2 = arrays[8]
        val p1 = DefaultPoint(5)
        val p2 = DefaultPoint(3, 0)
        val p3 = DefaultPoint(2, 5)

        assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr1.at(p1)
        }
        assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr2.at(p2)
        }

        val caught = assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr2.at(p3)
        }
        println(caught.message)
    }

    @Test
    fun ndArraySet() {
        val arrays = sampleNDArrays()
        arrays[0].set(indexToPoint(0, arrays[0].getShape()), 777)
        assertTrue(arrays[0].at(indexToPoint(0, arrays[0].getShape())) == 777)
        arrays[5].set(indexToPoint(2, arrays[5].getShape()), 43)
        assertTrue(arrays[5].at(indexToPoint(2, arrays[5].getShape())) == 43)
        arrays[7].set(indexToPoint(8, arrays[7].getShape()), 0)
        assertTrue(arrays[7].at(indexToPoint(8, arrays[7].getShape())) == 0)
        arrays[8].set(indexToPoint(0, arrays[8].getShape()), -22)
        assertTrue(arrays[8].at(indexToPoint(0, arrays[8].getShape())) == -22)
        arrays[9].set(indexToPoint(14, arrays[9].getShape()), 666)
        assertTrue(arrays[9].at(indexToPoint(14, arrays[9].getShape())) == 666)
    }

    @Test
    fun ndArraySetIllegalDimension() {
        val arrays = sampleNDArrays()
        val arr1 = arrays[6]
        val arr2 = arrays[8]
        val p1 = DefaultPoint(0, 0)
        val p2 = DefaultPoint(3, 0, 3)
        val p3 = DefaultPoint(2)

        assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr1.set(p1, 0)
        }
        var caught: Throwable = assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr2.set(p2, 0)
        }
        println(caught.message)
        caught = assertThrows<NDArrayException.IllegalPointDimensionException> {
            arr2.set(p3, 0)
        }
        println(caught.message)
    }

    @Test
    fun ndArraySetIllegalCoordinate() {
        val arrays = sampleNDArrays()
        val arr1 = arrays[6]
        val arr2 = arrays[8]
        val p1 = DefaultPoint(5)
        val p2 = DefaultPoint(3, 0)
        val p3 = DefaultPoint(2, 5)

        assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr1.set(p1, 0)
        }
        assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr2.set(p2, 0)
        }

        val caught = assertThrows<NDArrayException.IllegalPointCoordinateException> {
            arr2.set(p3, 0)
        }
        println(caught.message)
    }

    @Test
    fun ndArrayView() {
        val arr = sampleNDArrays()[7]
        val view = arr.view()
        val viewView = view.view()
        val viewViewView = viewView.view()
        val point = DefaultPoint(1, 3)
        arr.set(point, 888)
        assertTrue(arr.at(point) == 888)
        assertTrue(view.at(point) == 888)
        assertTrue(viewView.at(point) == 888)
        assertTrue(viewViewView.at(point) == 888)

        assertThrows<ClassCastException> {
            view as DefaultNDArray
        }
    }

    @Test
    fun ndArrayAddSameDimension() {
        val arrays = sampleNDArrays()
        arrays[0].add(arrays[1])
        assertTrue(arrays[0].checkIdentical(arrays[2]))
        arrays[5].add(arrays[4])
        assertTrue(arrays[5].checkIdentical(arrays[6]))
        arrays[7].add(arrays[8])
        assertTrue(arrays[7].checkIdentical(arrays[9]))

        val a = zeros(DefaultShape(100))
        val b = ones(DefaultShape(100))
        val c = ones(DefaultShape(100))

        b.add(a)
        assertTrue(c.checkIdentical(b))

        a.add(b)
        assertTrue(a.checkIdentical(b))
        assertTrue(a.checkIdentical(c))

        val aa = zeros(DefaultShape(10, 1, 3, 4))
        val bb = ones(DefaultShape(10, 1, 3, 4))
        val cc = ones(DefaultShape(10, 1, 3, 4))

        bb.add(aa)
        assertTrue(cc.checkIdentical(bb))

        aa.add(bb)
        assertTrue(aa.checkIdentical(bb))
        assertTrue(aa.checkIdentical(cc))
    }

    @Test
    fun ndArrayAddDifferentDimensions() {
        val arrays = sampleNDArrays()
        arrays[9].add(arrays[3])
        assertTrue(arrays[9].checkIdentical(arrays[10]))

        val a = zeros(DefaultShape(2, 3, 5, 7))
        val b = ones(DefaultShape(2, 3, 5))
        val c = ones(DefaultShape(2, 3, 5, 7))

        a.add(b)
        assertTrue(a.checkIdentical(c))
    }

    @Test
    fun ndArrayAddWrongDimension() {
        val a = zeros(DefaultShape(4, 5, 6))

        var b = zeros(DefaultShape(4, 5, 6, 7))
        var caught = assertThrows<Exception> {
            a.add(b)
        }
        println(caught)

        b = zeros(DefaultShape(4))
        caught = assertThrows<Exception> {
            a.add(b)
        }
        println(caught)
    }

    @Test
    fun ndArrayAddWrongSize() {
        val a = zeros(DefaultShape(4, 5, 6))

        var b = zeros(DefaultShape(4, 4))
        var caught = assertThrows<Exception> {
            a.add(b)
        }
        println(caught)

        b = zeros(DefaultShape(4, 5, 7))
        caught = assertThrows<Exception> {
            a.add(b)
        }
        println(caught)
    }

    @Test
    fun ndArrayDotMatrices() {
        val arrays = sampleNDArrays()
        var res = arrays[11].dot(arrays[12])
        assertTrue(arrays[13].checkIdentical(res))

        val a = zeros(DefaultShape(1, 10))
        val b = ones(DefaultShape(10, 20))
        val c = zeros(DefaultShape(1, 20))
        val d = ones(DefaultShape(20, 30))
        val eShape = DefaultShape(10, 30)
        val e = zeros(eShape)
        for (i in 0 until e.size) {
            e.set(indexToPoint(i, eShape), 20)
        }
        res = a.dot(b)
        assertTrue(res.checkIdentical(c))
        res = b.dot(d)
        assertTrue(res.checkIdentical(e))
    }

    @Test
    fun ndArrayDotMatrixVector() {
        val arrays = sampleNDArrays()
        var res = arrays[8].dot(arrays[4])
        assertTrue(arrays[14].checkIdentical(res))

        res = zeros(DefaultShape(40, 30)).dot(ones(DefaultShape(30)))
        assertTrue(res.checkIdentical(zeros(DefaultShape(40, 1))))

        res = ones(DefaultShape(20, 25)).dot(ones(DefaultShape(25)))
        val aShape = DefaultShape(20, 1)
        val a = zeros(aShape)
        for (i in 0 until a.size) {
            a.set(indexToPoint(i, aShape), 25)
        }
        assertTrue(res.checkIdentical(a))
    }

    @Test
    fun ndArrayDotNonConsistentMatrices() {
        val a = zeros(DefaultShape(4, 5))

        var b = zeros(DefaultShape(4, 4))
        val caught = assertThrows<Exception> {
            a.dot(b)
        }
        println(caught)

        b = zeros(DefaultShape(6, 5))
        assertThrows<Exception> {
            a.add(b)
        }

        b = zeros(DefaultShape(1, 1))
        assertThrows<Exception> {
            a.add(b)
        }
    }

    @Test
    fun ndArrayDotWrongDimension() {
        val shapes = sampleShapes()
        val b = zeros(DefaultShape(1))
        for (shape in shapes) {
            val caught = assertThrows<Exception> {
                zeros(shape).dot(b)
            }
            println(caught)
        }
    }

    @Test
    fun ndArrayNdim() {
        val shapes = sampleShapes()
        for (shape in shapes) {
            assertEquals(zeros(shape).dimNumber, shape.dimNumber)
        }
    }

    @Test
    fun ndArrayDim() {
        val shapes = sampleShapes()
        for (shape in shapes) {
            for (d in 0 until shape.dimNumber) {
                assertEquals(zeros(shape).dim(d), shape.dim(d))
            }
        }
    }

    @Test
    fun ndArraySize() {
        val shapes = sampleShapes()
        for (shape in shapes) {
            assertEquals(zeros(shape).size, shape.size)
        }
    }

    /*
     *
     * Utils
     *
     */

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            println()
            println("Exception messages:")
            println()
        }

        @JvmStatic
        @AfterAll
        fun destoy() {
            println()
            println("End of exception messages")
            println()
        }

        private fun indexToPoint(index: Int, shape: Shape): Point {
            val coords = IntArray(shape.dimNumber)
            var cur = index
            for (i in shape.dimNumber - 1 downTo 0) {
                coords[i] = cur % shape.dim(i)
                cur /= shape.dim(i)
            }
            return DefaultPoint(*coords)
        }

        private fun gen1DArray(arr: IntArray): DefaultNDArray {
            val shape = DefaultShape(arr.size)
            val ndArray = zeros(shape)
            for (i in arr.indices) {
                ndArray.set(indexToPoint(i, shape), arr[i])
            }
            return ndArray
        }

        private fun gen2DArray(arr: Array<IntArray>): DefaultNDArray {
            val shape = DefaultShape(arr.size, arr[0].size)
            val ndArray = zeros(shape)
            for (i in arr.indices) {
                for (j in arr[i].indices) {
                    ndArray.set(indexToPoint(i * arr[i].size + j, shape), arr[i][j])
                }
            }
            return ndArray
        }

        private fun NDArray.checkIdentical(other: NDArray): Boolean {
            if (other.dimNumber != dimNumber) {
                return false
            }
            for (i in 0 until dimNumber) {
                if (dim(i) != other.dim(i)) {
                    return false
                }
            }
            val dims = IntArray(other.dimNumber) { id -> dim(id) }
            val shape = DefaultShape(*dims)
            for (id in 0 until size) {
                if (at(indexToPoint(id, shape)) != other.at(indexToPoint(id, shape))) {
                    return false
                }
            }
            return true
        }

        private fun samplePoints() = arrayOf(
            DefaultPoint(),
            DefaultPoint(5),
            DefaultPoint(-1, -2, -3, -4, -5),
            DefaultPoint(*(IntArray(1000) {1}))
        )

        private fun sampleShapes() = arrayOf(
            DefaultShape(5),
            DefaultShape(1, 2, 3, 4, 5),
            DefaultShape(10, 11, 8),
            DefaultShape(*(IntArray(1000) {1}))
        )

        private fun sample1DArrays(): Array<NDArray> = arrayOf(
            gen1DArray(intArrayOf(-1)),
            gen1DArray(intArrayOf(1)),
            gen1DArray(intArrayOf(0)),
            gen1DArray(intArrayOf(-111, -222, -333)),
            gen1DArray(intArrayOf(3, 4, 5, 6, 7)),
            gen1DArray(intArrayOf(-4, 5, 3, 2, 0)),
            gen1DArray(intArrayOf(-1, 9, 8, 8, 7))
        )

        private fun sample2DArrays(): Array<NDArray> = arrayOf(
            gen2DArray(arrayOf(
                intArrayOf(3, 4, 5, 6, 7),
                intArrayOf(-1, -3, 0, 2, 4),
                intArrayOf(0, -31, 33, 20, -7)
            )),
            gen2DArray(arrayOf(
                intArrayOf(1, 2, 3, 4, 5),
                intArrayOf(-1, -2, -3, -4, -5),
                intArrayOf(0, 0, 0, 0, 0)
            )),
            gen2DArray(arrayOf(
                intArrayOf(4, 6, 8, 10, 12),
                intArrayOf(-2, -5, -3, -2, -1),
                intArrayOf(0, -31, 33, 20, -7)
            )),
            gen2DArray(arrayOf(
                intArrayOf(-107, -105, -103, -101, -99),
                intArrayOf(-224, -227, -225, -224, -223),
                intArrayOf(-333, -364, -300, -313, -340)
            )),
            gen2DArray(arrayOf(
                intArrayOf(4, 6, 0),
                intArrayOf(-2, -5, 7),
            )),
            gen2DArray(arrayOf(
                intArrayOf(2, 1, 3, 1),
                intArrayOf(-3, 0, -3, 7),
                intArrayOf(-9, 2, 2, -1)
            )),
            gen2DArray(arrayOf(
                intArrayOf(-10, 4, -6, 46),
                intArrayOf(-52, 12, 23, -44),
            )),
            gen2DArray(arrayOf(
                intArrayOf(85),
                intArrayOf(-85),
                intArrayOf(0)
            ))
        )

        private fun sampleNDArrays() = sample1DArrays().plus(sample2DArrays())

        private fun NDArray.getShape(): Shape = DefaultShape(*(IntArray(dimNumber) { id -> dim(id) }))
    }
}