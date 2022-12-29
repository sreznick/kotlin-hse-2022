
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
class DefaultNDArray private constructor(
    private val shape: Shape,
    private val value: Int = 0,
    private val values : IntArray = IntArray(shape.size).apply {fill(value)}) : NDArray {

    companion object {
        fun ones(shape : Shape) : DefaultNDArray {
            return DefaultNDArray(shape, 1)
        }
        fun zeros(shape : Shape) : DefaultNDArray {
            return DefaultNDArray(shape, 0)
        }
    }

    private fun getIndex(point: Point): Int {
        var index = 0
        var part = 1
        for(i in 0 until point.ndim) {
            val j = point.ndim - 1 - i
            if (point.dim(j) > shape.dim(j)) {
                throw NDArrayException.IllegalPointCoordinateException()
            }
            index += part * point.dim(j)
            part *= shape.dim(j)
        }
        return index
    }

    override fun at(point: Point): Int {
        if (shape.ndim != point.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }
        return values[getIndex(point)]
    }

    override fun set(point: Point, value: Int) {
        if (shape.ndim != point.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }
        values[getIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(shape, value, values.copyOf())

    override fun view(): NDArray = DefaultNDArray(shape, value, values)

    override fun add(other: NDArray) {
        if(other.ndim == ndim) {
            chechDemantions(other)
            if (other is DefaultNDArray) {
                for (i in 0 until values.size) {
                    values[i] += other.values[i]
                }
            } else {
                throw NDArrayException.IllegalNDArrayClassException()
            }
        } else if (other.ndim == ndim - 1) {
            if (other is DefaultNDArray) {
                for (i in 0 until size) {
                    val j = i % other.size
                    values[i] += other.values[j]
                }
            } else {
                throw NDArrayException.IllegalNDArrayClassException()
            }
        } else {
            throw NDArrayException.IllegalPointDimensionException()
        }
    }

    private fun chechDemantions(other: NDArray) {
        for (i in 0 until other.ndim) {
            if (dim(i) != other.dim(i)) {
                throw NDArrayException.IllegalPointDimensionException()
            }
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || other.ndim > 2) {
            throw NDArrayException.IllegalPointDimensionException()
        } else if (other.ndim == 2) {
            if (dim(1) != other.dim(0)) {
                throw NDArrayException.IllegalPointDimensionException()
            }
            val newSize = other.dim(1) * dim(0)
            val newValues : IntArray = IntArray(newSize).apply {fill(0)}
            for (i in 0 until dim(0)) {
                for (j in 0 until other.dim(1)) {
                    for (k in 0 until dim(1)) {
                        newValues[i * dim(0) + j] += values[i * dim(1) + k] * (other as DefaultNDArray).values[j + k * other.dim(0)]
                    }
                }
            }
            return DefaultNDArray(DefaultShape(dim(0), other.dim(1)), 0, newValues)
        } else {
            return DefaultNDArray(DefaultShape(dim(0), other.dim(1)), 0)
        }
    }

    override val size: Int
        get() = shape.size
    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)
}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException() : NDArrayException()
    class IllegalPointDimensionException() : NDArrayException()
    class IllegalNDArrayClassException() : NDArrayException()
}