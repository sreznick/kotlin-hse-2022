
interface NDArray: SizeAware, DimensionAware {
    /**
     * Получаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun at(point: Point): Int

    /**
     * Устанавливаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun set(point: Point, value: Int)

    /**
     * Копируем текущий NDArray
     *
     */
    fun copy(): NDArray

    /**
     * Создаем view для текущего NDArray
     *
     * Ожидается, что будет создан новая реализация  интерфейса.
     * Но она не должна быть видна в коде, использующем эту библиотеку как внешний артефакт
     *
     * Должна быть возможность делать view над view.
     *
     * In-place-изменения над view любого порядка видна в оригнале и во всех view
     *
     * Проблемы thread-safety игнорируем
     */
    fun view(): NDArray

    /**
     * In-place сложение
     *
     * Размерность other либо идентична текущей, либо на 1 меньше
     * Если она на 1 меньше, то по всем позициям, кроме "лишней", она должна совпадать
     *
     * Если размерности совпадают, то делаем поэлементное сложение
     *
     * Если размерность other на 1 меньше, то для каждой позиции последней размерности мы
     * делаем поэлементное сложение
     *
     * Например, если размерность this - (10, 3), а размерность other - (10), то мы для три раза прибавим
     * other к каждому срезу последней размерности
     *
     * Аналогично, если размерность this - (10, 3, 5), а размерность other - (10, 5), то мы для пять раз прибавим
     * other к каждому срезу последней размерности
     */
    fun add(other: NDArray)

    /**
     * Умножение матриц. Immutable-операция. Возвращаем NDArray
     *
     * Требования к размерности - как для умножения матриц.
     *
     * this - обязательно двумерна
     *
     * other - может быть двумерной, с подходящей размерностью, равной 1 или просто вектором
     *
     * Возвращаем новую матрицу (NDArray размерности 2)
     *
     */
    fun dot(other: NDArray): NDArray
}

/**
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val array: IntArray, private val dims: IntArray) : NDArray {
    override val ndim: Int = dims.size - 1
    override val size: Int
        get() = dims[0]

    private constructor(shape: Shape, defaultValue: Int)
            : this(IntArray(shape.size) { defaultValue }, IntArray(shape.ndim + 1) { 1 }) {
        for (i in shape.ndim - 1 downTo 0)
            dims[i] = dims[i + 1] * shape.dim(i)
    }

    private fun getExactIndex(point: Point): Int {
        if (ndim != point.ndim)
            throw NDArrayException.IllegalPointDimensionException()
        var ind = 0
        for (i in 0 until ndim) {
            if (point.dim(i) !in 0 until dim(i))
                throw NDArrayException.IllegalPointCoordinateException()
            ind += point.dim(i) * dims[i + 1]
        }
        return ind
    }

    private fun getPointIndex(i: Int, trunc: Int): Point {
        if (i >= size)
            throw NDArrayException.IllegalIntCoordinateException()
        var num = i / dims[ndim - trunc]
        val coordinates = IntArray(ndim - trunc)
        for (j in coordinates.size - 1 downTo 0) {
            coordinates[j] = num % dim(j)
            num /= dim(j)
        }
        return DefaultPoint(*coordinates)
    }

    companion object {
        fun ones(shape: Shape): NDArray = DefaultNDArray(shape, 1)

        fun zeroes(shape: Shape): NDArray = DefaultNDArray(shape, 0)
    }

    override fun dim(i: Int): Int = dims[i] / dims[i + 1]

    override fun at(point: Point): Int = array[getExactIndex(point)]

    override fun set(point: Point, value: Int) {
        array[getExactIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(array.copyOf(), dims.copyOf())

    override fun view(): NDArray = DefaultNDArrayViewer(this)

    override fun add(other: NDArray) {
        val diff = ndim - other.ndim
        if (diff !in 0..1)
            throw NDArrayException.IncompatibleArgumentsException()
        for (i in 0 until other.ndim)
            if (dim(i) != other.dim(i))
                throw NDArrayException.IncompatibleArgumentsException()
        for (i in array.indices)
            array[i] += other.at(getPointIndex(i, diff))
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim > 2 || dim(1) != other.dim(0))
            throw NDArrayException.IncompatibleArgumentsException()
        val n = dim(0)
        val l = dim(1)
        val m = when (other.ndim) { 1 -> 1 else -> other.dim(1) }
        val newDims = intArrayOf(m * n, m, 1)
        val newArray = IntArray(newDims[0])
        for (i in 0 until n)
            for (j in 0 until m)
                for (k in 0 until l)
                    newArray[i * m + j] += array[i * l + k] * other.at(
                        if (other.ndim == 2) DefaultPoint(k, j) else DefaultPoint(k)
                    )
        return DefaultNDArray(newArray, newDims)
    }
}

class DefaultNDArrayViewer(val a: NDArray) : NDArray by a

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalIntCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
    class IncompatibleArgumentsException : NDArrayException()
}
