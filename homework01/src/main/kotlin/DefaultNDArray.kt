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

class DefaultNDArray private constructor(val shape: DefaultShape, private val elements: IntArray) : NDArray {
    override val size: Int
        get() = elements.size
    override val ndim: Int
        get() = shape.ndim
    private val shapeMultiplied = IntArray(shape.ndim) { 1 }

    init {
        shapeMultiplied[0] = (1 until shape.ndim).fold(1) { a, i -> a * shape.dim(i) }
        (1 until shape.ndim).map {
            shapeMultiplied[it] = shapeMultiplied[it - 1] / shape.dim(it)
        }
    }


    override fun at(point: Point): Int {
        checkPointIsCorrect(point)
        return elements[get1DPositionFromPoint(point)]
    }

    override fun set(point: Point, value: Int) {
        checkPointIsCorrect(point)
        elements[get1DPositionFromPoint(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, elements.clone())

    override fun view(): NDArray = DefaultNDArray(shape, elements)

    override fun add(other: NDArray) {
        when (ndim - other.ndim) {
            0 -> {
                (0 until ndim).map {
                    if (dim(it) != other.dim(it)) throw NDArrayException.IllegalNDArrayDimensionException(other)
                }
                for (i in 0 until size) {
                    elements[i] += other.at(getPointFromPosition(i, ndim))
                }
            }
            1 -> {
                (other.ndim - 1 downTo 0).map {
                    if (dim(it + 1) != other.dim(it)) throw NDArrayException.IllegalNDArrayDimensionException(other)
                }
                for (i in 0 until size / dim(0)) {
                    for (j in 0 until dim(0)) {
                        elements[i * dim(0) + j] += other.at(getPointFromPosition(i, ndim - 1))
                    }
                }
            }
            else -> throw NDArrayException.IllegalNDArrayDimensionException(other)
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim != 2 || dim(1) != other.dim(0)) {
            throw NDArrayException.IllegalNDArrayDimensionException(other)
        }
        val newShape = DefaultShape(dim(0), other.dim(1))
        val newElements = IntArray(newShape.dim(0) * newShape.dim(1)) { 0 }
        var ind = 0
        for (i in (0 until newShape.dim(0))) {
            for (j in 0 until newShape.dim(1)) {
                for (k in 0 until dim(1)) {
                    newElements[ind] += this.at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                }
                ind++
            }
        }
        return DefaultNDArray(newShape, newElements)
    }

    override fun dim(i: Int): Int = shape.dim(i)

    private fun checkPointIsCorrect(point: Point) {
        if (point.ndim != ndim) throw NDArrayException.IllegalPointDimensionException(point)
        (0 until ndim).map {
            if (point.dim(it) >= dim(it))
                throw NDArrayException.IllegalPointCoordinateException(point)
        }
    }

    private fun get1DPositionFromPoint(point: Point): Int {
        return (0 until shape.ndim).fold(0) { acc, i -> acc + shapeMultiplied[i] * point.dim(i) }
    }

    private fun getPointFromPosition(i: Int, dims: Int): Point {
        var clonedI = i
        val pointCoordinates =
            IntArray(dims) { (clonedI / shapeMultiplied[it]).also { _ -> clonedI %= shapeMultiplied[it] } }
        return DefaultPoint(*pointCoordinates)
    }

    companion object {
        fun zeros(shape: DefaultShape): NDArray {
            val size = (0 until shape.ndim).fold(1) { acc, i -> acc * shape.dim(i) }
            return DefaultNDArray(shape, IntArray(size) { 0 })
        }

        fun ones(shape: DefaultShape): NDArray {
            val size = (0 until shape.ndim).fold(1) { acc, i -> acc * shape.dim(i) }
            return DefaultNDArray(shape, IntArray(size) { 1 })
        }
    }
}


sealed class NDArrayException(reason: String = "") : Exception(reason) {

    class IllegalPointCoordinateException(point: Point) : NDArrayException(
        reason = "Point coordinates are: ${
            (0 until point.ndim).map { i ->
                point.dim(i)
            }.toIntArray().contentToString()
        }"
    )

    class IllegalPointDimensionException(point: Point) :
        NDArrayException(reason = "Number of dimensions is invalid: ${point.ndim}")

    class IllegalNDArrayDimensionException(array: NDArray) : NDArrayException(
        reason = "Incompatible dimension: ${
            (0 until array.ndim).map { i ->
                array.dim(
                    i
                )
            }.toIntArray().contentToString()
        }\""
    )

}
