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
open class DefaultNDArray private constructor(private val shape: Shape, private val points: IntArray) : NDArray {
    override val ndim: Int = shape.ndim
    override val size: Int = points.size

    companion object {
        fun ones(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size) { 1 })
        fun zeros(shape: Shape): NDArray = DefaultNDArray(shape, IntArray(shape.size) { 0 })
    }

    override fun dim(i: Int): Int = shape.dim(i)
    override fun at(point: Point): Int = points[findIndex(point)]
    override fun copy(): NDArray = DefaultNDArray(shape, points.clone())
    override fun view(): NDArray = DefaultNDArray(this.shape, this.points)

    override fun set(point: Point, value: Int) {
        points[findIndex(point)] = value
    }

    override fun add(other: NDArray) {
        if (other.ndim + 1 != ndim && other.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException(other.ndim, shape.ndim)
        }
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException(i, other.dim(i), shape.dim(i))
            }
        }
        var curIndex: Point = DefaultPoint(*IntArray(other.ndim) { 0 })
        val lastDim = if (ndim != other.ndim) dim(ndim - 1) else 1
        for (i in 0 until lastDim) {
            for (j in 0 until other.size) {
                points[i + j * lastDim] += other.at(curIndex)

                val defaultShape = DefaultShape(*(0 until other.ndim).map { other.dim(it) }.toIntArray())
                val sizeShape = defaultShape.ndim - 1
                var indexVar = 1
                val coords = IntArray(curIndex.ndim) { 0 }

                for (k in sizeShape downTo 0) {
                    val curDimInd = curIndex.dim(k)
                    val shapeDimInd = defaultShape.dim(k)
                    coords[k] = (curDimInd + indexVar) % shapeDimInd
                    indexVar = (curDimInd + indexVar) / shapeDimInd
                }
                curIndex = DefaultPoint(*coords)
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim > 2) {
            throw NDArrayException.IllegalPointDimensionException(other.ndim, shape.ndim)
        }

        if (other.dim(0) != dim(1)) {
            throw NDArrayException.IllegalPointCoordinateException(0, other.dim(0), shape.dim(1))
        }
        val lastDim = if (other.ndim == 1) 1 else other.dim(1)
        val result = zeros(DefaultShape(dim(0), lastDim))

        for (i in 0 until dim(0)) {
            for (j in 0 until lastDim) {
                var sum = 0
                for (k in 0 until dim(1)) {
                    val point = if (other.ndim == 1) DefaultPoint(k) else DefaultPoint(k, j)
                    sum += at(DefaultPoint(i, k)) * other.at(point)
                }
                result.set(DefaultPoint(i, j), sum)
            }
        }
        return result
    }

    private fun findIndex(point: Point): Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException(point.ndim, ndim)
        }
        var index: Int = point.dim(0)
        for (i in 1 until point.ndim) {
            index = dim(i) * index + point.dim(i)
        }
        if (index >= size) {
            throw NDArrayException.IllegalPointCoordinateException(index, point.dim(index), point.dim(index))
        }
        return index
    }
}

sealed class NDArrayException(reason: String) : Exception(reason) {
    class IllegalPointCoordinateException(dim: Int, point: Int, shape: Int)
        : NDArrayException("Exception: Illegal Point Coordinate at $dim, expected $shape, got $point")
    class IllegalPointDimensionException(pointDim: Int, shapeDim: Int)
        : NDArrayException("Exception: Illegal Point Dimension: expected $shapeDim, got $pointDim")
}