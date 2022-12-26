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
     * Аналогично, если размерность this - (10, 3, 5), а размерность other - (10, 3), то мы для пять раз прибавим
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

    fun convertTo1D(): IntArray
}


/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val data: IntArray, private val dimensions: IntArray) : NDArray {
    companion object {
        fun zeros(shape: Shape): DefaultNDArray {
            return initDefault(shape, 0)
        }

        fun ones(shape: Shape): DefaultNDArray {
            return initDefault(shape, 1)
        }

        private fun initDefault(shape: Shape, baseValue : Int) : DefaultNDArray {
            return DefaultNDArray(IntArray(shape.size) { baseValue }, IntArray(shape.ndim) { shape.dim(it) })
        }

    }

    override val size: Int = dimensions.reduce { acc, it -> acc * it }

    override val ndim: Int
        get() = dimensions.size

    override fun dim(i: Int) = dimensions[i]

    private fun get1dIndex(point: Point): Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException(point.ndim, ndim)
        }
        val badIndex = (0 until ndim).indexOfFirst { point.dim(it) < 0 || point.dim(it) >= dim(it) }
        if (badIndex != -1) {
            throw NDArrayException.IllegalPointCoordinateException(badIndex)
        }
        var curSize = size
        return dimensions.indices.fold(0) { acc, it ->
            curSize /= dim(it)
            acc + curSize * point.dim(it)
        }
    }


    override fun at(point: Point) = data[get1dIndex(point)]

    override fun set(point: Point, value: Int) {
        data[get1dIndex(point)] = value
    }

    override fun copy() = DefaultNDArray(this.data.clone(), this.dimensions.clone())

    override fun view() = ModifiedNDArray(this)

    override fun add(other: NDArray) {
        require(ndim - other.ndim in 0..1 && (0 until other.ndim).all { dim(it) == other.dim(it) })
        val otherData = other.convertTo1D()
        if (ndim == other.ndim) {
            for (i in 0 until size) data[i] += otherData[i]
        } else {
            val lastDim = dim(ndim - 1)
            val blockSize = size / lastDim
            for (i in 0 until lastDim) {
                for (j in 0 until blockSize) {
                    data[i + j * lastDim] += otherData[j]
                }
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        require(ndim == 2 && (other.ndim in 1..2 && dim(1) == other.dim(0)))
        val thisRows = dim(0)
        val thisColumns = dim(1)
        val otherColumns = other.dim(other.ndim - 1)
        val result = zeros(DefaultShape(thisRows, otherColumns))
        for (i in 0 until thisRows) {
            for (j in 0 until otherColumns) {
                val scalar = (0 until thisColumns).fold(0) { acc, it ->
                    acc + at(DefaultPoint(i, it)) *
                            other.at(if (other.ndim != 1) DefaultPoint(it, j) else DefaultPoint(it))
                }
                result.set(DefaultPoint(i, j), scalar)
            }
        }
        return result
    }


    override fun convertTo1D(): IntArray {
        return data.clone()
    }

}

sealed class NDArrayException(message: String) : Exception(message) {
    data class IllegalPointCoordinateException(val index: Int) :
        NDArrayException("Illegal Point coordinate at index $index")

    data class IllegalPointDimensionException(val pointDim: Int, val dim: Int) :
        NDArrayException("Expected Point dimension to be ${dim},got $pointDim")
}