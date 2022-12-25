import java.lang.IllegalArgumentException

interface NDArray : SizeAware, DimensionAware {
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

    fun atIndex(index: Int): Int
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val NDArrayData: IntArray, private val NDArrayShape: DefaultShape) :
    NDArray {

    companion object {
        fun zeros(shape: DefaultShape): DefaultNDArray = DefaultNDArray(IntArray(shape.size), shape)
        fun ones(shape: DefaultShape): DefaultNDArray = DefaultNDArray(IntArray(shape.size) { 1 }, shape)

        private fun getPointByIndex(index : Int, other: NDArray) : Point {
            var tmpIndex = index
            val result = IntArray(other.ndim)
            var blockSize = other.size
            for (i in 0 until other.ndim) {
                blockSize /= other.dim(i)
                result[i] = tmpIndex / blockSize
                tmpIndex %= blockSize
            }
            return DefaultPoint(*result)
        }
    }

    override val size: Int
        get() = NDArrayData.size
    override val ndim: Int
        get() = NDArrayShape.ndim

    override fun dim(i: Int) = NDArrayShape.dim(i)

    private fun getIndex(point: Point): Int {
        if (ndim != point.ndim) {
            throw NDArrayException.IllegalPointDimensionException(ndim, point.ndim)
        }
        var index = 0
        var currBlock = size
        for (i in 0 until ndim) {
            // point.dim(i) > 0 (checked in point constructor)
            if (point.dim(i) >= dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException(dim(i), point.dim(i))
            }
            currBlock /= dim(i)
            index += currBlock * point.dim(i)
        }
        return index
    }

    override fun at(point: Point) = NDArrayData[getIndex(point)]

    override fun set(point: Point, value: Int) {
        NDArrayData[getIndex(point)] = value
    }

    override fun copy() = DefaultNDArray(NDArrayData.copyOf(), NDArrayShape)

    override fun view() = this

    override fun atIndex(index: Int): Int {
        if (index in 0 until size) {
            return NDArrayData[index]
        } else throw IllegalArgumentException("index $index is not in NDArray size - $size")
    }

    override fun add(other: NDArray) {
        if (ndim != other.ndim && ndim != other.ndim + 1) {
            throw NDArrayException.IllegalAddNDArrayDimensionsException(ndim, other.ndim)
        }
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw NDArrayException.NotMatchedAddNDArrayDimensions(i, dim(i), other.dim(i))
            }
        }
        var indexInThis = 0
        while (indexInThis < size) {
            for (indexInOther in 0 until other.size) {
                NDArrayData[indexInThis + indexInOther] += other.at(getPointByIndex(indexInOther, other))
            }
            indexInThis += other.size
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim > 2) {
            throw NDArrayException.IllegalDotNDArrayDimensionsException(ndim, other.ndim)
        }
        if (dim(1) != other.dim(0)) {
            throw NDArrayException.IllegalDotArrayDimensionException(dim(1), other.dim(0))
        }
        var result2Dim = 1
        if (other.ndim == 2) {
            result2Dim = other.dim(1)
        }
        val result = zeros(DefaultShape(dim(0), result2Dim))
        for (i in 0 until dim(0)) {
            for (j in 0 until result2Dim) {
                var tmp = 0
                for (k in 0 until dim(1)) {
                    tmp += at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                }
                result.set(DefaultPoint(i, j), tmp)
            }
        }
        return result
    }
}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    /* TODO: реализовать требуемые исключения */
    // IllegalPointCoordinateException
    // IllegalPointDimensionException
    class IllegalPointDimensionException(NDArrayDimension: Int, PointDimension: Int) :
        NDArrayException("NDArray Dimension is $NDArrayDimension when Point dimension is $PointDimension")

    class IllegalPointCoordinateException(NDArrayCoordinateDimension: Int, PointCoordinate: Int) :
        NDArrayException("NDArray Dimension is $NDArrayCoordinateDimension when Point coordinate is $PointCoordinate")

    class IllegalAddNDArrayDimensionsException(NDArrayNDim: Int, otherNDArrayNDim: Int) :
        NDArrayException("NDArray ndim is $NDArrayNDim when other NDArray ndim is $otherNDArrayNDim")

    class NotMatchedAddNDArrayDimensions(DimNum: Int, NDArrayDim: Int, otherNDArrayDim: Int) :
        NDArrayException("Dimension num $DimNum not matching: NDArrayDim - $NDArrayDim, otherNDArrayDim - $otherNDArrayDim")

    class IllegalDotNDArrayDimensionsException(NDArrayNDim: Int, otherNDArrayNDim: Int) :
        NDArrayException("NDArray ndim is $NDArrayNDim when other NDArray ndim is $otherNDArrayNDim")

    class IllegalDotArrayDimensionException(NDArray2Dim: Int, otherNDArray1Dim: Int) :
        NDArrayException("NDArray 2dim is $NDArray2Dim when other NDArray 1dim is $otherNDArray1Dim")
}