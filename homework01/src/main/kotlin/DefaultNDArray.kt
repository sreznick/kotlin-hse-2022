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
class DefaultNDArray private constructor(
    override val size: Int,
    override val ndim: Int,
    private val shape: Shape,
    private val array: IntArray
) :
    NDArray {

    private fun getIndexByPoint(point: Point): Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException(point)
        }
        var index = 0
        for (i in 0 until point.ndim) {
            if (point.dim(i) >= shape.dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException(point)
            }
            var k = 1
            for (j in i + 1 until point.ndim)
                k *= shape.dim(j)
            index += k * point.dim(i)
        }
        return index
    }

    private fun getPointByIndex(index: Int): Point {
        var curIndex = index
        var curDim = 1
        val coords = IntArray(ndim)
        for (i in ndim - 1 downTo 0) {
            curDim *= dim(i)
            coords[i] = curIndex % curDim
            curIndex /= curDim
        }
        return DefaultPoint(*coords)
    }

    override fun at(point: Point): Int {
        return array[getIndexByPoint(point)]
    }

    override fun set(point: Point, value: Int) {
        array[getIndexByPoint(point)] = value
    }

    override fun copy(): NDArray {
        return DefaultNDArray(size, ndim, shape, array.copyOf())
    }

    override fun view(): NDArray {
        return DefaultNDArray(size, ndim, shape, array)
    }

    override fun add(other: NDArray) {
        if (other.ndim == ndim) {
            for (i in 0 until size) {
                array[i] += other.at(getPointByIndex(i))
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        val newSize = shape.dim(0) * other.dim(1)
        val newArray = IntArray(newSize)
        val newShape = DefaultShape(shape.dim(0), other.dim(1))
        for (i in 0 until shape.dim(0)) {
            for (j in 0 until other.dim(1)) {
                for (k in 0 until other.dim(0)) {
                    newArray[getIndexByPoint(DefaultPoint(i, j))] +=
                        at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                }
            }
        }
        return DefaultNDArray(newSize, shape.dim(0), newShape, newArray)
    }

    override fun dim(i: Int): Int = shape.dim(i)

    companion object {
        fun zeros(shape: Shape): DefaultNDArray {
            return DefaultNDArray(shape.size, shape.ndim, shape, IntArray(shape.size))
        }

        fun ones(shape: Shape): DefaultNDArray {
            return DefaultNDArray(shape.size, shape.ndim, shape, IntArray(shape.size) { 1 })
        }
    }
}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    class IllegalPointDimensionException(point: Point) : NDArrayException("Wrong point $point dimensions")

    class IllegalPointCoordinateException(point: Point) : NDArrayException("Wrong point $point coordinate")
}