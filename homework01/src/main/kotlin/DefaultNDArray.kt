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
class DefaultNDArray : NDArray {
    private val shape: Shape
    private val array: IntArray

    private constructor(shape: Shape, value: Int) {
        array = IntArray(shape.size, fun(_) = value)
        this.shape = shape
    }

    private constructor(shape: Shape, array: IntArray) {
        this.array = array
        this.shape = shape
    }

    companion object {
        fun ones(shape: Shape): DefaultNDArray = DefaultNDArray(shape, 1)

        fun zeros(shape: Shape): DefaultNDArray = DefaultNDArray(shape, 0)

    }

    private fun checkPoint(point: Point) {
        when (point.ndim) {
            shape.ndim -> for (j in 0 until point.ndim) {
                if (point.dim(j) !in 0 until shape.dim(j))
                    throw NDArrayException.IllegalPointCoordinateException()
            }

            else -> throw NDArrayException.IllegalPointDimensionException()
        }
    }

    private fun pointToIndex(point: Point): Int {
        checkPoint(point)
        var a = 1
        var index = 0
        for (i in 0 until point.ndim) {
            index += a * point.dim(i)
            a *= shape.dim(i)
        }
        return index
    }

    override fun at(point: Point): Int = array[pointToIndex(point)]

    override fun set(point: Point, value: Int) {
        array[pointToIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, array.clone())

    override fun view(): NDArray = object : NDArray by this {}
    private fun canAdd(other: NDArray): Boolean {
        if (ndim - other.ndim !in 0..1) {
            return false
        }
        for (i in 0 until ndim.coerceAtMost(other.ndim)) {
            if (dim(i) != other.dim(i)) {
                return false
            }
        }
        return true
    }

    override fun add(other: NDArray) {
        if (!canAdd(other)) throw NDArrayException.IllegalShapeOfSummandException()
        val coordinate = IntArray(shape.ndim) { 0 }
        for (i in 0 until shape.size) {
            val point: Point = DefaultPoint(*coordinate.sliceArray(0 until shape.ndim - ndim + other.ndim))
            array[i] += other.at(point)
            coordinate[0]++
            for (j in 0 until shape.ndim) {
                coordinate[(j + 1) % shape.ndim] += coordinate[j] / shape.dim(j)
                coordinate[j] %= shape.dim(j)
            }
        }
    }

    private fun canDot(other: NDArray): Boolean = (other.ndim <= ndim && ndim == 2 && dim(1) == other.dim(0))

    override fun dot(other: NDArray): NDArray {
        // this 2 3
        // other 3 4
        if (!canDot(other)) throw NDArrayException.IllegalShapeOfDotException()
        val dot = zeros(DefaultShape(dim(0), if (other.ndim == 1) 1 else other.dim(1) ))
        for (i in 0 until dot.dim(0))
            for (j in 0 until dot.dim(1))
                for (k in 0 until dim(1))
                    dot.set(DefaultPoint(i, j), dot.at(DefaultPoint(i, j)) + other.at( if (other.ndim == 2) DefaultPoint(k , j) else DefaultPoint(k)) * at(DefaultPoint(i, k)))
        return dot
    }

    override val size: Int
        get() = shape.size
    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)
}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()

    class IllegalShapeOfSummandException : NDArrayException()
    class IllegalShapeOfDotException : NDArrayException()
}