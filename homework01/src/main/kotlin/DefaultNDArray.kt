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
     * Ожидается, что будет создан новая реализация интерфейса.
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
class DefaultNDArray constructor(
    private val data: IntArray, private val shape: Shape
) : NDArray {

    companion object {
        fun ones(shape: Shape) = initWith(shape, 1)

        fun zeros(shape: Shape) = initWith(shape, 0)

        private fun initWith(shape: Shape, value: Int): NDArray {
            val data = IntArray(shape.size) { value }
            return DefaultNDArray(data, shape)
        }
    }

    private fun getIndex(point: Point): Int {
        if (point.ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }

        val multipliers = IntArray(point.ndim) { 1 }
        (point.ndim - 1 downTo 0).forEach {
            val dim = point.dim(it)
            if (dim < 0 || dim >= shape.dim(it)) {
                throw NDArrayException.IllegalPointCoordinateException()
            }
            if (it < point.ndim - 1) {
                multipliers[it] = multipliers[it + 1] * shape.dim(it + 1)
            }
        }

        return multipliers.foldIndexed(0) { index, acc, i -> acc + i * point.dim(index) }
    }

    override fun at(point: Point): Int {
        val index = getIndex(point)
        return data[index]
    }

    override fun set(point: Point, value: Int) {
        val index = getIndex(point)
        data[index] = value
    }

    override fun copy(): NDArray {
        val data = this.data.copyOf()
        val dimensions = IntArray(shape.ndim) { shape.dim(it) }
        return DefaultNDArray(data, DefaultShape(*dimensions))
    }

    override fun view(): NDArray {
        return this
//        return DefaultNDArray(data, shape) // или так?
    }

    override fun add(other: NDArray) {
        val dimenDiff = this.ndim - other.ndim
        if (dimenDiff !in arrayOf(0, 1)) {
            throw NDArrayException.BadDimensionException()
        }
        (0 until other.ndim).forEach {
            if (this.dim(it) != other.dim(it)) {
                throw NDArrayException.BadDimensionException()
            }
        }

        val thisCoordinates = IntArray(this.ndim) { 0 }
        var thisPoint = DefaultPoint(* thisCoordinates)
        var thatPoint = DefaultPoint(* thisCoordinates.dropLast(1).toIntArray())
        var level = 0


        if (dimenDiff == 0) {
            this.set(thisPoint, this.at(thisPoint) + other.at(thisPoint))
        } else {
            this.set(thisPoint, this.at(thisPoint) + other.at(thatPoint))
        }
        while (level != thisCoordinates.size) {
            if (thisCoordinates[level] == this.dim(level) - 1) {
                thisCoordinates[level] = 0
                level++
            } else {
                thisCoordinates[level]++
                thisPoint = DefaultPoint(*thisCoordinates)
                if (dimenDiff == 0) {
                    this.set(thisPoint, other.at(thisPoint) + this.at(thisPoint))
                } else {
                    thatPoint = DefaultPoint(* thisCoordinates.dropLast(1).toIntArray())
                    this.set(thisPoint, this.at(thisPoint) + other.at(thatPoint))
                }
                level = 0
            }
        }
    }


    override fun dot(other: NDArray): NDArray {
        if (this.ndim > 2 || other.ndim > 2) {
            throw NDArrayException.BadDimensionException()
        }
        if (this.dim(1) != other.dim(0)) {
            throw NDArrayException.BadDimensionException()
        }

        if (other.ndim == 1) {
            val shape = DefaultShape(this.dim(0))
            val newArray = zeros(shape)
            (0 until this.dim(0)).forEach { i ->
                var total = 0
                (0 until this.dim(1)).forEach { j ->
                    total += this.at(DefaultPoint(i, j)) * other.at(DefaultPoint(j))
                }
                newArray.set(DefaultPoint(i), total)
            }
            return newArray
        } else {
            val shape = DefaultShape(this.dim(0), other.dim(1))
            val newArray = zeros(shape)
            (0 until this.dim(0)).forEach { i ->
                (0 until this.dim(1)).forEach { j ->
                    (0 until other.dim(1)).forEach { k ->
                        val result = this.at(DefaultPoint(i, j)) * other.at(DefaultPoint(j, k))
                        newArray.set(DefaultPoint(i, k), newArray.at(DefaultPoint(i, k)) + result)
                    }
                }
            }
            return newArray
        }
    }

    override val size = data.size
    override val ndim = shape.ndim
    override fun dim(i: Int) = shape.dim(i)
}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
    class BadDimensionException : NDArrayException()
}