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
class DefaultNDArray private constructor(private val shape: Shape, private val values: IntArray) : NDArray {
    override val ndim: Int
        get() = shape.ndim

    override val size: Int
        get() = shape.size

    override fun dim(i: Int): Int = shape.dim(i)

    companion object {
        fun zeros(shape: Shape): NDArray {
            return DefaultNDArray(shape, IntArray(shape.size) { 0 })
        }

        fun ones(shape: Shape): NDArray {
            return DefaultNDArray(shape, IntArray(shape.size) { 1 })
        }
    }

    override fun at(point: Point): Int {
        validatePoint(point)
        return values[findIndex(point)]
    }

    override fun set(point: Point, value: Int) {
        validatePoint(point)
        values[findIndex(point)] = value
    }

    override fun copy(): NDArray {
        return DefaultNDArray(shape, values.copyOf())
    }

    override fun view(): NDArray {
        return DefaultNDArray(shape, values)
    }

    override fun add(other: NDArray) {
        if (ndim == other.ndim) {
            checkShapeEquality(other)
            var point: Point? = DefaultPoint(*IntArray(ndim) { 0 })
            while (point != null) {
                set(point, at(point) + other.at(point))
                point = getNextPoint(point)
            }
        } else if (ndim == other.ndim + 1) {
            val extraDimInd = getPositionOfExtraDimension(other)
            var point: Point? = DefaultPoint(*IntArray(ndim) { 0 })
            while (point != null) {
                set(point, at(point) + other.at(cutDimensionFromPoint(point, extraDimInd)))
                point = getNextPoint(point)
            }
        } else {
            throw NDArrayException.IllegalNDArrayDimensionException(
                "expected `this` and `other` to have same dimensions"
            )
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2) {
            throw NDArrayException.IllegalNDArrayDimensionException("expected 'this' to be two-dimensional");
        }
        return if (other.ndim == 2 && dim(1) == other.dim(0)) {
            matricesMult(other)
        } else if (other.ndim == 1 && this.dim(1) == other.dim(0)) {
            matricesMult(convert1DArrayTo2DArray(this.dim(0), 1, other))
        } else if (other.ndim == 1 && this.dim(1) == 1) {
            matricesMult(convert1DArrayTo2DArray(this.dim(0), other.dim(0), other))
        } else {
            throw NDArrayException.IllegalNDArrayDimensionException("expected `other` to be another dimensional")
        }
    }

    private fun findIndex(point: Point): Int {
        var indexInValues = 0
        var dimensionMult: Int = size
        for (index in 0 until point.ndim) {
            dimensionMult /= shape.dim(index)
            indexInValues += point.dim(index) * dimensionMult
        }
        return indexInValues
    }

    private fun getNextPoint(point: Point): Point? {
        val coords = IntArray(point.ndim)
        for (index in 0 until point.ndim) {
            coords[index] = point.dim(index)
        }
        var foundNext = false
        for (index in point.ndim - 1 downTo 0) {
            if (coords[index] < dim(index) - 1) {
                ++coords[index]
                foundNext = true
                for (innerIndex in index + 1 until point.ndim) {
                    coords[innerIndex] = 0
                }
                break
            }
        }
        return if (foundNext) DefaultPoint(*coords) else null
    }

    private fun getPositionOfExtraDimension(other: NDArray): Int {
        var extraDimension = -1
        for (thisIndex in 0 until ndim) {
            val otherIndex = thisIndex - (if (extraDimension != -1) 1 else 0)
            if (otherIndex == other.ndim) {
                extraDimension = thisIndex
                break
            }
            if (other.dim(otherIndex) != dim(thisIndex)) {
                if (extraDimension != -1) {
                    throw NDArrayException.IllegalNDArrayDimensionException(
                        "found more than one extra dimensions in this NDArray"
                    )
                }
                extraDimension = thisIndex
            }
        }
        if (extraDimension == -1) {
            throw NDArrayException.IllegalNDArrayDimensionException(
                "found no extra dimensions in this NDArray"
            )
        }
        return extraDimension
    }

    private fun cutDimensionFromPoint(point: Point, position: Int): Point {
        val coords = IntArray(point.ndim - 1)
        for (index in 0 until position) {
            coords[index] = point.dim(index)
        }
        for (index in position + 1 until point.ndim) {
            coords[index - 1] = point.dim(index)
        }
        return DefaultPoint(*coords)
    }

    private fun validatePoint(point: Point) {
        if (point.ndim != shape.ndim) {
            throw NDArrayException.IllegalPointDimensionException();
        }
        for (index in 0 until point.ndim) {
            if (point.dim(index) < 0 || point.dim(index) >= shape.dim(index)) {
                throw NDArrayException.IllegalPointCoordinateException()
            }
        }
    }

    private fun checkShapeEquality(other: NDArray) {
        for (index in 0 until ndim) {
            if (dim(index) != other.dim(index)) {
                val message = "expected `this` and `other` to have same dimensions"
                throw NDArrayException.IllegalNDArrayDimensionException(message)
            }
        }
    }

    private fun convert1DArrayTo2DArray(firstDim: Int, secondDim: Int, other: NDArray): NDArray {
        val ndArray = DefaultNDArray(DefaultShape(firstDim, secondDim), IntArray(other.size))
        for (index in 0 until other.size) {
            ndArray.values[index] = other.at(DefaultPoint(index))
        }
        return ndArray
    }

    private fun matricesMult(other: NDArray): NDArray {
        val result = DefaultNDArray(
            DefaultShape(this.dim(0), other.dim(1)),
            IntArray(this.dim(0) * other.dim(1))
        )
        for (i in 0 until this.dim(0)) {
            for (j in 0 until other.dim(1)) {
                var sum = 0
                for (k in 0 until this.dim(1)) {
                    sum += this.at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                }
                result.set(DefaultPoint(i, j), sum)
            }
        }
        return result
    }
}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    class IllegalPointCoordinateException(reason: String = "") : NDArrayException(reason)
    class IllegalPointDimensionException(reason: String = "") : NDArrayException(reason)
    class IllegalNDArrayDimensionException(reason: String = "") : NDArrayException(reason)
}