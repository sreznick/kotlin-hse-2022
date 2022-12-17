
interface NDArray: SizeAware, DimentionAware {
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

fun Point.toIndex(shape: Shape): Int {
    var index = 0
    var curSize = 1
    for (i in ndim - 1 downTo 0) {
        if (this.dim(i) >= shape.dim(i) || this.dim(i) < 0) {
            throw NDArrayException.IllegalPointCoordinateException(i, this.dim(i), shape.dim(i))
        }
        index += curSize * this.dim(i)
        curSize *= shape.dim(i)
    }
    return index
}

fun indexToPoint(index: Int, shape: Shape): Point {
    val coords = IntArray(shape.ndim)
    var cur = index
    for (i in shape.ndim - 1 downTo 0) {
        coords[i] = cur % shape.dim(i)
        cur /= shape.dim(i)
    }
    return DefaultPoint(*coords)
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val shape: Shape, private val baseArray: IntArray): NDArray {

    override fun at(point: Point): Int {
        if (point.ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException(point.ndim, shape.ndim)
        }
        return baseArray[point.toIndex(shape)]
    }

    override fun set(point: Point, value: Int) {
        if (point.ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException(point.ndim, shape.ndim)
        }
        baseArray[point.toIndex(shape)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, baseArray.copyOf())

    override fun view() = object : NDArray by this {}

    override fun add(other: NDArray) {
        if (other.ndim > ndim || other.ndim <= ndim - 2) {
            throw IllegalArgumentException("Cannot add an array ${other.ndim}-dimensional array to $ndim-dimensional")
        }
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw IllegalArgumentException("The size of dimension $i are different")
            }
        }
        if (ndim == other.ndim) {
            for (i in 0 until size) {
                baseArray[i] += other.at(indexToPoint(i, shape))
            }
        } else {
            val otherDims = IntArray(other.ndim) { id -> other.dim(id) }
            val otherShape = DefaultShape(*otherDims)
            for (i in 0 until other.size) {
                for (layer in 0 until shape.dim(shape.ndim - 1)) {
                    baseArray[i * shape.dim(shape.ndim - 1) + layer] += other.at(indexToPoint(i, otherShape))
                }
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2) {
            throw UnsupportedOperationException("Expected 2-dimensional array for a matrix product, found $ndim-dimensional")
        } else if (dim(1) != other.dim(0)) {
            throw UnsupportedOperationException("Matrices must be consistent")
        } else if (!(other.ndim in 1..2)) {
            throw IllegalArgumentException("Expected a 2 or 1, but found ${other.ndim}-dimensional array")
        }
        val resultDim2 = if (other.ndim == 2) other.dim(1) else 1
        val result = zeros(DefaultShape(dim(0), resultDim2))
        for (i in 0 until dim(0)) {
            for (j in 0 until resultDim2) {
                for (k in 0 until dim(1)) {
                    val point2 = if (other.ndim == 2) DefaultPoint(k, j) else DefaultPoint(k)
                    result.baseArray[i * resultDim2 + j] += baseArray[i * dim(1) + k] * other.at(point2)
                }
            }
        }
        return result
    }

    override val size: Int
        get() = shape.size

    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int {
        return shape.dim(i)
    }

    companion object {

        fun zeros(shape: Shape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size) {0})

        fun ones(shape: Shape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size) {1})
    }

}

sealed class NDArrayException(override val message: String?) : Exception(message) {
    /* TODO: реализовать требуемые исключения */
    // IllegalPointCoordinateException
    // IllegalPointDimensionException

    class IllegalPointCoordinateException(pos: Int, found: Int, expected: Int): NDArrayException(
        "The size of dimension ${pos + 1} is $expected, but point coordinate is ${found + 1}"
    )

    class IllegalPointDimensionException(found: Int, expected: Int): NDArrayException(
        "Point has $found dimensions, but expected $expected"
    )
}