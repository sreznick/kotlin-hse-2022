
interface NDArray : SizeAware, DimentionAware {
    /*
     * Получаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun at(point: Point): Int

    /*
     * Устанавливаем значение по индексу point
     *
     * Если размерность point не равна размерности NDArray
     * бросаем IllegalPointDimensionException
     *
     * Если позиция по любой из размерностей некорректна с точки зрения
     * размерности NDArray, бросаем IllegalPointCoordinateException
     */
    fun set(point: Point, value: Int)

    /*
     * Копируем текущий NDArray
     *
     */
    fun copy(): NDArray

    /*
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

    /*
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

    /*
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

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */


class DefaultNDArray(private val shape: Shape) : NDArray {
    override val size: Int = shape.size
    override val ndim: Int = shape.ndim
    private var nums = IntArray(size)
    private val prefixProduct = (0 until ndim).runningFold(1) { acc, i -> acc * shape.dim(i) }

    private fun getIndex(point: Point): Int {
        var index = 0
        for (i in 0 until point.ndim) {
            if (point.dim(i) >= shape.dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException("Coordinate mismatch")
            }
            index += point.dim(i) * prefixProduct[i]
        }
        return index
    }

    private val dimensionList
        get() = MutableList(ndim) { i -> shape.dim(i) }

    private fun addHelper(dimensions: List<Int>, list: MutableList<Int> = mutableListOf()): List<List<Int>> =
        buildList {
            if (list.size == dimensions.size) {
                add(list.toList())
                return this
            }
            for (i in 0 until dimensions[list.size]) {
                list.add(i)
                addAll(addHelper(dimensions, list))
                list.removeLast()
            }
        }
    private fun addIdentical(other: NDArray) {
        for (pointCoordinates in addHelper(dimensionList)) {
            val curPoint = DefaultPoint(*pointCoordinates.toIntArray())
            nums[getIndex(curPoint)] += other.at(curPoint)
        }
    }

    private fun addNotIdentical(other: NDArray) {
        for (pointCoordinates in addHelper(dimensionList)) {
            val curPoint = DefaultPoint(*pointCoordinates.toIntArray())
            val otherCurPoint = DefaultPoint(*pointCoordinates.dropLast(1).toIntArray())
            nums[getIndex(curPoint)] += other.at(otherCurPoint)
        }
    }

    private fun checkDimensions(other: NDArray) {
        if (!(0 until other.ndim).all { dim(it) == other.dim(it) }) {
            throw NDArrayException.IncompatibleArgumentsException("Dimensions of Arrays are incompatible")
        }
    }

    override fun add(other: NDArray) {
        if ((ndim - other.ndim) !in 0..1)
            throw NDArrayException.IncompatibleArgumentsException("Dimensions of Arrays are incompatible")
        checkDimensions(other)
        if (ndim == other.ndim)
            addIdentical(other)
        else
            addNotIdentical(other)
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim > 2 || dim(1) != other.dim(0)) {
            throw NDArrayException.IllegalArgumentsException("Dimensions are incorrect")
        }
        val finalShape = IntArray(2)
        finalShape[0] = dim(0)
        finalShape[1] = if (other.ndim == 1) 1 else other.dim(1)
        val res = ones(DefaultShape(*finalShape))
        val listOfPoints = addHelper(finalShape.toList())
        if (other.ndim == 2) {
            for (point in listOfPoints) {
                var value = 0
                for (k in 0 until shape.dim(1)) {
                    val first = DefaultPoint(*point.dropLast(1).toIntArray(), k)
                    val second = DefaultPoint(k, *point.drop(1).toIntArray())
                    value += at(first) * other.at(second)
                }
                res.set(DefaultPoint(*point.toIntArray()), value)
            }
        } else {
            for (point in listOfPoints) {
                var value = 0
                for (k in 0 until shape.dim(1)) {
                    val first = DefaultPoint(*point.dropLast(1).toIntArray(), k)
                    val second = DefaultPoint(k)
                    value += at(first) * other.at(second)
                }
                res.set(DefaultPoint(*point.toIntArray()), value)
            }
        }
        return res
    }
    override fun dim(i: Int) = shape.dim(i)
    override fun at(point: Point): Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException("Dimensions of point are unsuitable")
        }
        return nums[getIndex(point)]
    }

    override fun set(point: Point, value: Int) {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException("Dimensions of point are unsuitable")
        }
        nums[getIndex(point)] = value
    }

    override fun copy(): NDArray {
        val other = DefaultNDArray(shape)
        other.nums = nums.clone()
        return other
    }

    override fun view(): NDArray {
        return DefaultNDArrayView(this)
    }
    companion object {
        private fun init(i: Int, shape: Shape): NDArray {
            val res = DefaultNDArray(shape)
            res.nums.fill(i)
            return res
        }
        fun ones(shape: Shape): NDArray = init(1, shape)
        fun zeros(shape: Shape): NDArray = init(0, shape)
    }
}

class DefaultNDArrayView(array: NDArray) : NDArray by array

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException(message: String) : Exception(message)
    class IllegalPointDimensionException(message: String) : Exception(message)
    class IncompatibleArgumentsException(message: String) : Exception(message)
    class IllegalArgumentsException(message: String) : Exception(message)
}
