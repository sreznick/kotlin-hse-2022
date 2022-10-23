
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
class DefaultNDArray private constructor(private val shape: Shape,
                                         private val values: IntArray = IntArray(shape.size)): NDArray {

    override val size: Int = shape.size

    override val ndim: Int = shape.ndim

    override fun dim(i: Int): Int = shape.dim(i)

    companion object {

        private fun createAndFillNDArray(shape: Shape, value: Int): DefaultNDArray {
            val result = DefaultNDArray(shape)
            result.values.fill(value)
            return result
        }

        fun ones(shape: Shape): DefaultNDArray = createAndFillNDArray(shape, 1)

        fun zeros(shape: Shape): DefaultNDArray = createAndFillNDArray(shape, 0)

    }

    private fun pointToIndex(point: Point): Int {
        var dimensionsMultiplication = this.size
        var index = 0
        for (i in 0 until this.ndim) {
            dimensionsMultiplication /= this.dim(i)
            index += point.dim(i) * dimensionsMultiplication
        }
        return index
    }

    private fun indexToPoint(ndArray: NDArray, index: Int): Point {
        var indexCopy = index
        var dimensionsMultiplication = 1
        val coordinates = IntArray(ndArray.ndim)
        for (i in ndArray.ndim - 1 downTo 0) {
            dimensionsMultiplication *= ndArray.dim(i)
            coordinates[i] = indexCopy % dimensionsMultiplication
            indexCopy /= dimensionsMultiplication
        }
        return DefaultPoint(*coordinates)
    }

    private fun pointValidation(point: Point) {
        if (point.ndim != this.ndim) {
            throw NDArrayException.IllegalPointDimensionException("Expected ${this.ndim} but found ${point.ndim}")
        }
        for (i in 0 until this.ndim) {
            if (this.dim(i) <= point.dim(i) || point.dim(i) < 0) {
                throw NDArrayException.IllegalPointCoordinateException(i, point.dim(i))
            }
        }
    }

    override fun at(point: Point): Int {
        pointValidation(point)
        return this.values[pointToIndex(point)]
    }

    override fun set(point: Point, value: Int) {
        pointValidation(point)
        this.values[pointToIndex(point)] = value
    }

    override fun copy(): NDArray {
        return DefaultNDArray(this.shape, this.values.copyOf())
    }

    override fun view(): NDArray {
        return DefaultNDArray(this.shape, this.values)
    }

    private fun checkDimensionEqualityOrThrowException(other: NDArray) {
        for (i in 0 until this.ndim) {
            if (this.dim(i) != other.dim(i)) {
                throw NDArrayException.IllegalNDArrayDimensionsException(
                    "This and other array should have equal dimensions")
            }
        }
    }

    private fun findExtraDimensionOrThrowException(other: NDArray): Int {
        var indexOfExtraDimension = 0
        var foundExtraDimension = false
        for (indexOfDimensionInThis in 0 until this.ndim) {
            val indexOfDimensionInOther = indexOfDimensionInThis - (if (foundExtraDimension) 1 else 0)
            if (indexOfDimensionInOther == other.ndim) {
                indexOfExtraDimension = indexOfDimensionInThis
                foundExtraDimension = true
            } else if (this.dim(indexOfDimensionInThis) != other.dim(indexOfDimensionInOther) && foundExtraDimension) {
                throw NDArrayException.IllegalNDArrayDimensionsException(
                    "This NDArray should have not more than one extra dimension")
            } else if (this.dim(indexOfDimensionInThis) != other.dim(indexOfDimensionInOther)) {
                indexOfExtraDimension = indexOfDimensionInThis
                foundExtraDimension = true
            }
        }
        if (!foundExtraDimension) {
            throw NDArrayException.IllegalNDArrayDimensionsException("Expected in this NDArray one extra dimension")
        }
        return indexOfExtraDimension
    }

    override fun add(other: NDArray) {
        if (other.ndim != this.ndim && other.ndim != this.ndim - 1) {
            throw NDArrayException.IllegalNDArrayDimensionsException(
                "Other NDArray should have equal dimensions with this or should have one less for addition")
        }
        if (other.ndim == this.ndim) {
            checkDimensionEqualityOrThrowException(other)
            this.values.forEachIndexed { index, _ -> this.values[index] += other.at(indexToPoint(this, index)) }
        } else {
            val indexOfExtraDimension = findExtraDimensionOrThrowException(other)
            for (valueOfExtraDimension in 0 until this.dim(indexOfExtraDimension)) {
                var indexInOther = 0
                for (indexInThis in 0 until this.size) {
                    val curPoint = indexToPoint(this, indexInThis)
                    if (curPoint.dim(indexOfExtraDimension) != valueOfExtraDimension) {
                        continue
                    }
                    this.values[indexInThis] += other.at(indexToPoint(other, indexInOther))
                    indexInOther++
                }
            }
        }
    }

    private fun dot2DimensionsMatrices(other: NDArray): NDArray {
        val result = DefaultNDArray(DefaultShape(this.dim(0), other.dim(1)))
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

    private fun oneDimensionToTwo(other: NDArray, firstDimension: Int, secondDimension: Int) : NDArray {
        val result = DefaultNDArray(DefaultShape(firstDimension, secondDimension))
        for (i in 0 until other.size) {
            result.values[i] = other.at(DefaultPoint(i))
        }
        return result
    }

    override fun dot(other: NDArray): NDArray {
        if (this.ndim != 2) {
            throw NDArrayException.IllegalNDArrayDimensionsException(
                "This NDArray should have 2 dimensions for dot")
        }
        if (other.ndim == 2 && this.dim(1) == other.dim(0)) {
            return dot2DimensionsMatrices(other)
        } else if (other.ndim == 1 && (this.dim(1) == 1 || this.dim(1) == other.dim(0))) {
            if (this.dim(1) == other.dim(0)) {
                return dot2DimensionsMatrices(oneDimensionToTwo(other, other.dim(0), 1))
            }
            return dot2DimensionsMatrices(oneDimensionToTwo(other, 1, other.dim(0)))
        } else {
            throw NDArrayException.IllegalNDArrayDimensionsException("Not correct dimensions for dot")
        }
    }

}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
     class IllegalPointCoordinateException(val index: Int, val value: Int):
         NDArrayException("Illegal point coordinate on position ${index + 1} with value $value")
     class IllegalPointDimensionException(val reason: String): NDArrayException(reason)
     class IllegalNDArrayDimensionsException(val reason: String): NDArrayException(reason)
}
