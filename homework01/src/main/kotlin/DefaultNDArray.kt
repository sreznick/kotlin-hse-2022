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
}

/*
 * Базовая реализация NDArray
 *
 * Конструкторы должны быть недоступны клиенту
 *
 * Инициализация - через factory-методы ones(shape: Shape), zeros(shape: Shape) и метод copy
 */
class DefaultNDArray private constructor(private val defaultVal: Int, private val shape: Shape) : NDArray {

    private val arr: MutableMap<Point, Int> = HashMap()

    companion object {
        fun ones(shape: Shape) = DefaultNDArray(1, shape)

        fun zeros(shape: Shape) = DefaultNDArray(0, shape)
    }

    override fun at(point: Point): Int {
        validatePoint(point)
        return arr.getOrDefault(point, defaultVal)
    }

    override fun set(point: Point, value: Int) {
        validatePoint(point)
        arr[point] = value
    }

    override fun copy(): NDArray {
        val newNDArray = if (defaultVal == 1) {
            ones(shape)
        } else {
            zeros(shape)
        }
        arr.forEach { (point, res) -> newNDArray.set(point, res) }
        return newNDArray
    }

    override fun view(): NDArray {
        return this
    }

    private fun createNextPoint(prev: Point): Point {
        var savedIndex = -1
        for (i in 0 until prev.ndim) {
            if (prev.dim(i) < shape.dim(i) - 1) {
                savedIndex = i
                break
            }
        }
        if (savedIndex == -1) {
            return prev
        }
        return DefaultPoint(*IntArray(prev.ndim)
        { i ->
            if (i > savedIndex) {
                return@IntArray prev.dim(i)
            } else if (i == savedIndex) {
                return@IntArray prev.dim(i) + 1
            }
            return@IntArray 0
        })
    }

    override fun add(other: NDArray) {
        if (ndim == other.ndim) {
            var startPoint: Point = DefaultPoint(*IntArray(ndim) { 0 })
            var nextPoint: Point
            do {
                arr[startPoint] = at(startPoint) + other.at(startPoint)
                nextPoint = createNextPoint(startPoint)
                if (nextPoint == startPoint) {
                    break
                }
                startPoint = nextPoint
            } while (true)
        } else if (ndim - 1 == other.ndim && ndim >= 2) {
            for (i in 0 until ndim - 1) {
                if (dim(i) != other.dim(i)) {
                    throw NDArrayException.IllegalPointDimensionException();
                }
            }

            var startPoint: Point = DefaultPoint(*IntArray(ndim) { 0 })
            var nextPoint: Point
            do {
                val otherCurrentPoint = DefaultPoint(*IntArray(other.ndim) { indx -> startPoint.dim(indx) })
                set(startPoint, at(startPoint) + other.at(otherCurrentPoint))
                nextPoint = createNextPoint(startPoint)
                if (nextPoint == startPoint) {
                    break
                }
                startPoint = nextPoint
            } while (true)

        } else {
            throw NDArrayException.IllegalPointDimensionException();
        }
    }

    private fun convertVectorToMatrix(other: NDArray): NDArray {
        val res = zeros(DefaultShape(other.dim(0), 1))
        for (i in 0 until other.dim(0)) {
            res.set(DefaultPoint(i, 0), other.at(DefaultPoint(i)))
        }
        return res
    }

    override fun dot(other: NDArray): NDArray {
        // 0 - строки 1 - столбцы
        if (ndim != 2 || other.ndim < 1 || other.ndim > 2 || other.dim(0) != dim(1)) throw NDArrayException.IllegalPointCoordinateException()
        var second = other
        if (other.ndim == 1) {
            second = convertVectorToMatrix(other)
        }
        val resultNDArray: NDArray = zeros(DefaultShape(dim(0), second.dim(1)))
        for (i in 0 until dim(0)) {
            for (j in 0 until second.dim(1)) {
                var res = 0
                for (k in 0 until dim(1)) {
                    res += at(DefaultPoint(i, k)) * second.at(DefaultPoint(k, j))
                }
                resultNDArray.set(DefaultPoint(i, j), res)
            }
        }
        return resultNDArray
    }

    override val size: Int
        get() = shape.size
    override val ndim: Int
        get() = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)

    private fun validatePoint(point: Point) {
        if (point.ndim != shape.ndim) throw NDArrayException.IllegalPointDimensionException()
        for (i in 0 until point.ndim) {
            if (point.dim(i) >= shape.dim(i)) {
                throw NDArrayException.IllegalPointCoordinateException()
            }
        }
    }


}

sealed class NDArrayException : Exception() {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
}
