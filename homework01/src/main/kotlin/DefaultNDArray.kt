
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

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val shape: DefaultShape, private val values: IntArray): NDArray {

    companion object {
        fun zeros(shape: DefaultShape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size){ 0 })
        fun ones(shape: DefaultShape): DefaultNDArray = DefaultNDArray(shape, IntArray(shape.size){ 1 })
    }
    override fun at(point: Point): Int = values[pointToLinearIndex(point)]

    override fun set(point: Point, value: Int) {
        values[pointToLinearIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, values.clone())

    override fun view(): NDArray = DefaultNDArray(shape, values)

    override fun add(other: NDArray) {
        if (other.ndim != ndim && (other.ndim + 1) != ndim) {
            throw NDArrayException.IllegalNDArrayDimensionException()
        }

        var possibleNumOfIncorrectIndexes = if (other.ndim == ndim) 0 else 1
        var offset = 0
        var lastSameIndex: Int = 0
        for (i in 0 until ndim) {
            if (dim(i) != other.dim(i - offset)) {
                if (possibleNumOfIncorrectIndexes < 0) {
                    throw NDArrayException.IllegalNDArrayDimensionException()
                }

                possibleNumOfIncorrectIndexes--
                lastSameIndex = i - 1
                offset = 1
            }
        }

        TODO("Not yet implemented")
    }

    override fun dot(other: NDArray): NDArray {
        TODO("Not yet implemented")
    }

    override val size: Int = shape.size
    override val ndim: Int = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)

    private fun pointToLinearIndex(point: Point): Int {
        if (point.ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }

        var index = point.dim(0)
        for (i in 1 until point.ndim) {
            index = dim(i) * index + point.dim(i)
        }

        if (index >= size) {
            throw NDArrayException.IllegalPointCoordinateException()
        }

        return index
    }
}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException: NDArrayException()
    class IllegalPointDimensionException: NDArrayException()
    class IllegalNDArrayDimensionException: NDArrayException()
}