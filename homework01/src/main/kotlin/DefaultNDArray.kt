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
class DefaultNDArray private constructor(val shape: Shape) : NDArray {
    companion object {
        fun ones(shape: Shape): NDArray {
            val array = DefaultNDArray(shape)
            array.array.fill(1)
            return array
        }

        fun zeros(shape: Shape): NDArray {
            return DefaultNDArray(shape)
        }
    }

    override fun at(point: Point): Int {
        val index = getIndex(point)
        return array[index]
    }

    override fun set(point: Point, value: Int) {
        val index = getIndex(point)
        array[index] = value
    }

    override fun copy(): NDArray {
        val other = DefaultNDArray(shape)
        other.array = array.copyOf()
        return other
    }

    override fun view(): NDArray {
        return object : NDArray {
            override fun at(point: Point): Int = this@DefaultNDArray.at(point)

            override fun set(point: Point, value: Int) = this@DefaultNDArray.set(point, value)

            override fun copy(): NDArray = this@DefaultNDArray.copy()

            override fun view(): NDArray = this

            override fun add(other: NDArray) = this@DefaultNDArray.add(other)

            override fun dot(other: NDArray): NDArray = this@DefaultNDArray.dot(other)

            override val size: Int
                get() = this@DefaultNDArray.size
            override val ndim: Int
                get() = this@DefaultNDArray.ndim

            override fun dim(i: Int): Int = this@DefaultNDArray.dim(i)
        }
    }

    override fun add(other: NDArray) {
        // Сперва мы проверим, что эти массивы можно складывать.
        val lastDim = if (shape.ndim == other.ndim) shape.ndim else shape.ndim - 1
        for (i in 0 until lastDim) {
            if (shape.dim(i) != shape.dim(i)) {
                throw NDArrayException.NotCompatibleArrayDimensions()
            }
        }
        addImpl(other)
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2) {
            throw NDArrayException.IllegalDimensionForMultiplication("This should be 2-dimensional.")
        }
        if (other.ndim > 2) {
            throw NDArrayException.IllegalDimensionForMultiplication("Other should be at most 2-dimensional.")
        }
        val fst = dim(0)
        val snd = dim(1)
        val otherFst = other.dim(0)

        // 1 случай, умножаем на число
        if (otherFst == 1 && other.ndim == 1) {
            val result = DefaultNDArray(DefaultShape(fst, snd))
            result.array = array.map { x -> x * other.at(DefaultPoint(0)) }.toIntArray()
            return result
        }

        // 2 случай, умножение двух матриц.
        if (other.ndim == 2) {
            if (snd != otherFst) {
                throw NDArrayException.IllegalDimensionForMultiplication("Can't multiply these arrays.")
            }
            val result = DefaultNDArray(DefaultShape(fst, other.dim(1)))
            for (i in 0 until fst) {
                for (k in 0 until other.dim(1)) {
                    var cur = 0
                    for (j in 0 until snd) {
                        cur += at(DefaultPoint(i, j)) * other.at(DefaultPoint(j, k))
                    }
                    result.set(DefaultPoint(i, k), cur)
                }
            }
            return result
        }

        // Последний случай, матрица на ветктор
        if (snd != otherFst) {
            throw NDArrayException.IllegalDimensionForMultiplication("Can't multiply these arrays.")
        }
        val result = DefaultNDArray(DefaultShape(fst, 1))
        for (i in 0 until fst) {
            var cur = 0
            for (j in 0 until snd) {
                cur += at(DefaultPoint(i, j)) * other.at(DefaultPoint(j))
            }
            result.set(DefaultPoint(i, 0), cur)
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

    var array: IntArray = IntArray(shape.size)

    private fun getIndex(point: Point): Int {
        if (shape.ndim != point.ndim) {
            throw NDArrayException.IllegalPointDimensionException()
        }
        var index = 0
        var size = 1
        for (i in point.ndim - 1 downTo 0) {
            if (point.dim(i) >= shape.dim(i) || point.dim(i) < 0) {
                throw NDArrayException.IllegalPointCoordinateException()
            }
            index += point.dim(i) * size
            size *= shape.dim(i)
        }
        return index
    }

    private fun addImpl(other: NDArray, vararg indices: Int) {
        val size = indices.size
        if (ndim == size) {
            val point = DefaultPoint(*indices)
            val otherPoint = if (other.ndim < ndim) DefaultPoint(*indices.sliceArray(0 until (size - 1))) else point
            set(point, at(point) + other.at(otherPoint))
            return
        }
        for (i in 0 until dim(size)) {
            addImpl(other, *indices, i)
        }
    }
}

sealed class NDArrayException(reason: String = "") : Exception(reason) {
    class IllegalPointCoordinateException : NDArrayException()
    class IllegalPointDimensionException : NDArrayException()
    class NotCompatibleArrayDimensions : NDArrayException()
    class IllegalDimensionForMultiplication(reason: String = "") : NDArrayException(reason)
}
