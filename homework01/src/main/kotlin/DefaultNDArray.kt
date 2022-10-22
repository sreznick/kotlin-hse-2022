import java.lang.Integer.min

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
class DefaultNDArray private constructor(val data: IntArray, private val shape: Shape) : NDArray {
    companion object {
        fun zeros(shape: Shape) : NDArray{
            return DefaultNDArray(IntArray(shape.size), shape)
        }

        fun ones(shape: Shape) : NDArray{
            return DefaultNDArray(IntArray(shape.size) {1}, shape)
        }
    }

    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int {
        return shape.dim(i)
    }

    override val size: Int
        get() = data.size

    fun getIndex(point: Point) : Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }
        var index = 0
        var currBlockSize : Int = size
        for (i in 0 until point.ndim) {
            if (point.dim(i) < 0 || point.dim(i) >= shape.dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException();
            }
            currBlockSize /= shape.dim(i)
            index += point.dim(i) * currBlockSize
        }
        return index;
    }

    override fun at(point: Point): Int {
        return data[getIndex(point)];
    }

    override fun set(point: Point, value: Int) {
        data[getIndex(point)] = value
    }

    override fun copy(): NDArray {
        return DefaultNDArray(data.copyOf(), shape)
    }

    override fun view(): NDArray {
        return this
    }

    fun addArray(other: NDArray, index1: Int, dims2: IntArray, blockSize1: Int, kdim: Int) {
        if (kdim < ndim) {
            val bs1 = blockSize1 / dim(kdim)
            val minDim = if (kdim < other.ndim) min(dim(kdim), other.dim(kdim)) else dim(kdim)
            for(i in 0 until minDim) {
                val idx1 = index1 + i * bs1
                if (kdim < other.ndim) dims2[kdim] = i
                addArray(other, idx1, dims2.copyOf(), bs1, kdim + 1)
            }
        } else {
            data[index1] += other.at(DefaultPoint(*dims2))
        }
    }

    override fun add(other: NDArray) {
        if (ndim == other.ndim || ndim == other.ndim + 1) {
            addArray(other, 0, IntArray(other.ndim), size, 0);
        } else {
            throw NDArrayException.IllegalNDArrayDimensionException();
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2 || dim(1) != other.dim(0)) {
            throw  NDArrayException.IllegalNDArrayDimensionException()
        } else {
            val res = zeros(if (other.ndim > 1) DefaultShape(dim(0), other.dim(1)) else DefaultShape(dim(0)))
            for(i in 0 until dim(0)) {
                for(j in 0 until other.dim(1)) {
                    var value = 0
                    for(k in 0 until dim(1)) {
                        value += this.at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                    }
                    res.set(DefaultPoint(i, j), value)
                }
            }
            return res
        }
    }

    override fun toString(): String {
        return "DefaultNDArray(data=${data.contentToString()})"
    }

}

sealed class NDArrayException : Exception() {
    /* TODO: реализовать требуемые исключения */
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
    class IllegalNDArrayDimensionException : NDArrayException()
}