
interface NDArray: SizeAware, DimentionAware {

    fun at(point: Point): Int

    fun set(point: Point, value: Int)

    fun copy(): NDArray

    fun view(): NDArray

    fun add(other: NDArray)

    fun dot(other: NDArray): NDArray

    fun getOnIndex(index: Int): Int
}

class DefaultNDArray private constructor(private val shape: Shape, private val points: IntArray): NDArray {
    override val ndim: Int
        get() = shape.ndim

    override val size: Int
        get() = shape.size

    override fun dim(i: Int): Int = shape.dim(i);

    override fun copy(): NDArray {
        return DefaultNDArray(shape, points.copyOf());
    }

    override fun view(): NDArray {
        return DefaultNDArray(shape, points);
    }

    companion object {
        fun ones(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size) {1})
        fun zeros(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size) {0})
    }

    private fun findIndexOfPoint(point: Point): Int {
        if (ndim != point.ndim) {
            throw NDArrayException.IllegalPointDimensionException(point);
        }
        var index: Int = 0
        var dimMul: Int = size
        for (idx in 0 until point.ndim) {
            if (point.dim(idx) !in 0 until shape.dim(idx)) {
                throw NDArrayException.IllegalPointCoordinateException(point)
            }
            dimMul /= shape.dim(idx);
            index += point.dim(idx) * dimMul
        }
        return index
    }

    override fun getOnIndex(index: Int): Int = points.get(index)

    override fun at(point: Point): Int = points[findIndexOfPoint(point)]
    override fun set(point: Point, value: Int) {
        points[findIndexOfPoint(point)] = value
    }
    override fun add(other: NDArray) {
        if (other.ndim == ndim - 1) {
            val sz: Int = size / shape.dim(ndim - 1)
            for (i in 0 until shape.dim(ndim - 1)) {
                for (j in 0 until sz) {
                    points[i * sz + j] += other.getOnIndex(j)
                }
            }
        }
        for (i in 0 until size) {
            points[i] += other.getOnIndex(i)
        }
    }

    private fun correctShapesMultiply(other: NDArray): Boolean {
        return shape.dim(1) == other.dim(0) && other.ndim <= 2 && ndim == 2;
    }

    override fun dot(other: NDArray): NDArray {
        if (!correctShapesMultiply(other)) {
            throw NDArrayException.IllegalShapeMultiplySizes(ndim, other.ndim)
        }
        val res = DefaultNDArray.zeros(DefaultShape(shape.dim(0), other.dim(1)))

        for (i in 0 until shape.dim(0)) {
            for (j in 0 until other.dim(1)) {
                var cur = 0
                for (k in 0 until shape.dim(1)) {
                    cur += at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                }
                res.set(DefaultPoint(i, j), cur)
            }
        }
        return res
    }
}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    class IllegalPointCoordinateException(point: Point) : NDArrayException("Illegal coordinate in point: $point")
    class IllegalPointDimensionException(point: Point) : NDArrayException("Illegal dimension in point: $point")
    class IllegalShapeMultiplySizes(firstSize: Int, secondSize: Int) :
        NDArrayException("Incorrect multiply shapes of $firstSize and $secondSize dimensions")
}
