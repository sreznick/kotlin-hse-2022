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
class DefaultNDArray private constructor(number: Int = 0, private val shape: Shape) : NDArray {

    override val ndim: Int = shape.ndim
    override fun dim(i: Int): Int = shape.dim(i)
    override val size: Int = shape.size

    private var array: IntArray = IntArray(size) { number }

    private fun convertIndexToPoint(index: Int): Point {
        var k = index
        val pointCoordinates = IntArray(this.ndim)
        for (i in 0 until this.ndim) {
            val dimProduct = this.shape.getDimensionsProductStartsWithIndex(i)
            pointCoordinates[i] = k / dimProduct
            k %= dimProduct
        }
        return DefaultPoint(*pointCoordinates)
    }

    private fun convertPointToArrayIndex(point: Point): Int =
        (0 until point.ndim)
            .fold(0) { acc, i -> acc + point.dim(i) * shape.getDimensionsProductStartsWithIndex(i) }


    private fun elementHasSameDimensions(element: DimentionAware): Boolean {
        for (i in 0 until ndim) if (this.dim(i) != element.dim(i)) return false
        return element.ndim == this.ndim
    }

    private fun pointHasCorrectCoordinates(point: Point): Boolean {
        for (i in 0 until point.ndim) if (point.dim(i) > this.dim(i)) return false
        return true
    }

    private fun pointHasIncorrectCoordinates(point: Point): Boolean = !pointHasCorrectCoordinates(point)

    private fun assertPointHasCorrectCoordinates(point: Point) {
        for (i in 0 until point.ndim) if (point.dim(i) > this.dim(i))
            throw NDArrayException.IllegalPointCoordinateException(i, point.dim(i), this.dim(i))
    }

    constructor(number: Int = 0, shape: Shape, arrayToCopy: IntArray) : this(number, shape) {
        this.array = arrayToCopy
    }

    override fun at(point: Point): Int {
        if (point.ndim != this.ndim) throw NDArrayException.IllegalPointDimensionException(point.ndim, this.ndim)
        assertPointHasCorrectCoordinates(point)
        return array[convertPointToArrayIndex(point)]
    }

    override fun set(point: Point, value: Int) {
        if (point.ndim != this.ndim) throw NDArrayException.IllegalPointDimensionException(point.ndim, this.ndim)
        assertPointHasCorrectCoordinates(point)
        array[convertPointToArrayIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape = this.shape, arrayToCopy = this.array.copyOf())

    companion object {
        fun ones(shape: Shape): NDArray = DefaultNDArray(number = 1, shape = shape)
        fun zeros(shape: Shape): NDArray = DefaultNDArray(number = 0, shape = shape)
    }

    internal inner class DefaultNDArrayView : NDArray {
        override val ndim: Int
            get() = this@DefaultNDArray.ndim

        override fun dim(i: Int) = this@DefaultNDArray.dim(i)
        override val size: Int
            get() = this@DefaultNDArray.size

        override fun at(point: Point): Int = this@DefaultNDArray.at(point)
        override fun set(point: Point, value: Int) = this@DefaultNDArray.set(point, value)
        override fun copy(): NDArray = this@DefaultNDArray.copy()
        override fun view(): NDArray = this@DefaultNDArray.view()
        override fun add(other: NDArray) = this@DefaultNDArray.add(other)
        override fun dot(other: NDArray): NDArray = this@DefaultNDArray.dot(other)
    }

    override fun view(): NDArray = DefaultNDArrayView()

    override fun add(other: NDArray) {
        when {
            elementHasSameDimensions(other) -> for (i in array.indices) array[i] += other.at(convertIndexToPoint(i))
            (this.ndim - 1 == other.ndim) && (0 until other.ndim).all { this.dim(it) == other.dim(it) } ->
                for (i in array.indices) array[i] += other.at(convertIndexToPoint((i + 1) / (size / other.size)))
            else -> throw NDArrayException.IllegalNDArrayDimensionException(
                "It's only possible to make \"add\" between NDArrays with dimensions differ by no more than one"
            )
        }
    }

    override fun dot(other: NDArray): NDArray {

        if (this.ndim != 2)
            throw NDArrayException.IllegalNDArrayDimensionException("It's only possible to multiply 2d-arrays")
        if (other.ndim > 2 || other.dim(0) != this.dim(1))
            throw NDArrayException.IllegalNDArrayDimensionException(
                "It's only possible to multiply matrices if their dimensions are compatible"
            )
        val otherSecondDim = if (other.ndim > 1) other.dim(1) else 1
        val matrixProduct = zeros(DefaultShape(dim(0), otherSecondDim))
        for (i in 0 until dim(0)) {
            for (j in 0 until otherSecondDim) {
                var matrixProductValue = 0
                for (k in 0 until this.dim(1)) {
                    matrixProductValue += at(DefaultPoint(i, k)) *
                            if (other.ndim > 1) other.at(DefaultPoint(k, j)) else other.at(DefaultPoint(k))
                }
                matrixProduct.set(point = DefaultPoint(i, j), value = matrixProductValue)
            }
        }
        return matrixProduct
    }
}

sealed class NDArrayException(reason: String = "Unknown") : Exception(reason) {
    class IllegalPointCoordinateException(index: Int, pointDim: Int, ndArrayDim: Int) :
        NDArrayException(
            "Point coordinate number $index is greater than $index'ths dimension of the NDArray ($pointDim > $ndArrayDim)"
        )

    class IllegalPointDimensionException(pointNDim: Int, ndArrayNDim: Int) : NDArrayException(
        "The number of point coordinates should be equal to the dimension of the NDArray (now: $pointNDim != $ndArrayNDim)"
    )

    class IllegalNDArrayDimensionException(reason: String) : NDArrayException(reason)
}