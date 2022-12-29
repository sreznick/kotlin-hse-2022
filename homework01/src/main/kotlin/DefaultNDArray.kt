interface NDArray: SizeAware, DimentionAware {

    fun at(point: Point): Int
    fun set(point: Point, value: Int)
    fun copy(): NDArray
    fun view(): NDArray
    fun add(other: NDArray)
    fun dot(other: NDArray): NDArray
}

class DefaultNDArray private constructor(private val array: IntArray, private val prefDimentions: IntArray) : NDArray {
    override val ndim = prefDimentions.size - 1
    override val size = prefDimentions[0]

    private constructor(shape: Shape, intVal: Int)
            : this(IntArray(shape.size) { intVal }, IntArray(shape.ndim + 1) { 1 }) {
        for (i in shape.ndim - 1 downTo 0)
            prefDimentions[i] = prefDimentions[i + 1] * shape.dim(i)
    }

    companion object {
        fun ones(shape: Shape) = DefaultNDArray(shape, 1)
        fun zeros(shape: Shape) = DefaultNDArray(shape, 0)
    }

    override fun dim(i: Int) = prefDimentions[i] / prefDimentions[i + 1]

    private fun getCoordinates(value: Int, trunc: Int): Point {
        val coordinates = IntArray(ndim - trunc)
        var number = value / prefDimentions[ndim - trunc]
        for (i in coordinates.size - 1 downTo 0) {
            coordinates[i] = number % dim(i)
            number /= dim(i)
        }
        return DefaultPoint(*coordinates)
    }

    private fun getNumber(point: Point): Int {
        if (ndim != point.ndim) throw NDArrayException.IllegalPointDimensionException()
        var number = 0;
        for (i in 0 until ndim) {
            if (!(0 <= point.dim(i) && point.dim(i) <= dim(i))) throw NDArrayException.IllegalPointCoordinateException()
            number += prefDimentions[i + 1] * point.dim(i)
        }
        return number
    }

    override fun at(point: Point) = array[getNumber(point)]

    override fun set(point: Point, value: Int) {
        array[getNumber(point)] = value
    }

    override fun copy() = DefaultNDArray(array.copyOf(), prefDimentions.copyOf())

    override fun view(): NDArray = DefaultNDArrayViewer(this)

    override fun add(other: NDArray) {
        for (i in array.indices) array[i] += other.at(getCoordinates(i, ndim - other.ndim))
    }

    override fun dot(other: NDArray): NDArray {

        if (maxOf(ndim, other.ndim) > 2 || ndim != other.ndim) throw NDArrayException.IllegalPointDotException()

        val n = dim(0)
        val m: Int
        if (other.ndim == 1) m = 1 else m = other.dim(1);
        val k: Int
        if (ndim == 1) k = 1 else k = dim(1)

        val resultDimentions = intArrayOf(n * m, m, 1)
        val resultArray = IntArray(resultDimentions[0])

        for (i in 0 until n)
            for (j in 0 until m)
                for (l in 0 until k)
                    resultArray[i * m + j] +=
                        other.at(if (other.ndim == 1) DefaultPoint(l) else DefaultPoint(l, j)) * array[i * k + l]
        return DefaultNDArray(resultArray, resultDimentions)
    }
}

class DefaultNDArrayViewer(val a: NDArray) : NDArray by a

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
    class IllegalPointDotException : NDArrayException()
}