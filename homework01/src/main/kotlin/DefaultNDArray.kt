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
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val array: IntArray, private val shape: Shape) : NDArray {

    val folded: IntArray
        get() {
            val folded = IntArray(shape.ndim)
            var acc = 1
            for (i in shape.ndim - 1 downTo 0) {
                folded[i] = acc
                acc *= shape.dim(i)
            }
            return folded
        }

    private fun checkPoint(point: Point) {
        if (point.ndim != shape.ndim) throw NDArrayException.IllegalPointDimensionException(point.ndim, shape.ndim)
        for (i in 0 until point.ndim) {
            if (point.dim(i) > shape.dim(i) || point.dim(i) < 0) {
                throw NDArrayException.IllegalPointCoordinateException(i, point.dim(i), shape.dim(i))
            }
        }
    }

    private fun indexByPoint(point: Point): Int {
        checkPoint(point)
        var index = 0
        for (i in 0 until point.ndim) {
            index += point.dim(i) * folded[i]
        }
        return index
    }

    override fun at(point: Point): Int = array[indexByPoint(point)]

    override fun set(point: Point, value: Int) {
        array[indexByPoint(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(array.clone(), shape)

    override fun view(): NDArray {
        val view: NDArray by this
        return view
    }

    private operator fun getValue(ignore: Any?, ignore_: Any): NDArray {
        return this
    }


    private fun checkSum(other: NDArray) {
        if (!(other.ndim == ndim || other.ndim + 1 == ndim)) {
            throw NDArrayException.IllegalNDArrayAddTermDimensions(ndim, other.ndim)
        }
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw NDArrayException.IllegalNDArrayAddTermShape(i, dim(i), other.dim(i))
            }
        }
    }

    override fun add(other: NDArray) {
        checkSum(other)
        val step = if (other.ndim == ndim) 1 else dim(ndim - 1)
        for (i in 0 until other.size) {
            val elem = other.at(getPointForOther(other, pointByIndex(i * step)))
            for (j in i * step until (i + 1) * step) {
                array[j] += elem
            }
        }
    }

    private fun getPointForOther(other: NDArray, point: Point): Point {
        return if (other.ndim == ndim) {
            point
        } else {
            val pointArray = IntArray(other.ndim)
            for (i in 0 until other.ndim) {
                pointArray[i] = point.dim(i)
            }
            DefaultPoint(*pointArray)
        }
    }

    private fun pointByIndex(index: Int): Point {
        var indexVar = index
        val pointArray = IntArray(ndim)
        for (i in 0 until ndim) {
            pointArray[i] = indexVar / folded[i]
            indexVar -= pointArray[i] * folded[i]
        }
        return DefaultPoint(*pointArray)
    }

    override fun dot(other: NDArray): NDArray {
        checkMultiply(other)
        if (other.ndim == 1 && other.dim(0) == 1) {
            return DefaultNDArray(array.map { it * other.at(DefaultPoint(0)) }.toIntArray(), shape)
        }
        val otherCols = if (other.ndim == 2) other.dim(1) else 1
        val result = zeros(DefaultShape(dim(0), otherCols))
        for (i in 0 until dim(0)) {
            for (j in 0 until otherCols) {
                var sum = 0
                for (z in 0 until dim(1)) {
                    sum += at(DefaultPoint(i, z)) * other.at(
                        if (other.ndim == 2) DefaultPoint(z, j) else DefaultPoint(z)
                    )
                }
                result.set(DefaultPoint(i, j), sum)
            }
        }
        return result
    }

    private fun checkMultiply(other: NDArray) {
        if (other.ndim > 2 || other.ndim < 1 || ndim != 2) {
            throw NDArrayException.IllegalNDArrayDotTermDimensions(ndim, other.ndim)
        } else if (other.dim(0) != dim(1)) {
            throw NDArrayException.IllegalNDArrayDotTermShape(dim(1), other.dim(0))
        }
    }

    override val size: Int
        get() = shape.size
    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)

    companion object {
        fun ones(shape: Shape): DefaultNDArray {
            return DefaultNDArray(IntArray(shape.size) { 1 }, shape)
        }

        fun zeros(shape: Shape): DefaultNDArray {
            return DefaultNDArray(IntArray(shape.size), shape)
        }
    }
}

sealed class NDArrayException(reason: String) : IllegalArgumentException(reason) {
    class IllegalPointCoordinateException(dim: Int, point: Int, shape: Int) :
        NDArrayException("On coordinate $dim point has $point dim, but only $shape dims in shape")

    class IllegalPointDimensionException(pointDim: Int, shapeDim: Int) :
        NDArrayException("Point has $pointDim dimensions, but shape has only $shapeDim")

    class IllegalNDArrayAddTermDimensions(itNdim: Int, otherNdim: Int) :
        NDArrayException("Other NDArray has unsupported ndim: $otherNdim, may be $itNdim or ${itNdim - 1}")

    class IllegalNDArrayAddTermShape(dim: Int, itDim: Int, otherDim: Int) :
        NDArrayException("Other NDArray has not equal shape on dim $dim: expect $itDim, actual $otherDim")

    class IllegalNDArrayDotTermDimensions(itNdim: Int, otherNdim: Int) :
        NDArrayException("This dimensions expected 2, actual $itNdim. Other dimensions expected 1 or 2, actual $otherNdim")

    class IllegalNDArrayDotTermShape(itCol: Int, otherRows: Int) :
        NDArrayException("Matrix with $itCol columns can't dot on matrix with $otherRows rows")
}