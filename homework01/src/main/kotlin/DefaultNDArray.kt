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
class DefaultNDArray private constructor(private val data: IntArray, private val shape: Shape) : NDArray {

    private val blockSize = IntArray(shape.ndim)

    init {
        blockSize[shape.ndim - 1] = 1
        for (i in shape.ndim - 1 downTo 1) {
            blockSize[i - 1] = blockSize[i] * shape.dim(i)
        }
    }

    private fun pointToIndex(point: Point): Int {
        if (point.ndim != ndim) {
            throw NDArrayException.IllegalPointDimensionException(ndim, point.ndim)
        }
        (0 until point.ndim).forEach {
            if (point.dim(it) < 0 || point.dim(it) >= shape.dim(it))
                throw NDArrayException.IllegalPointCoordinateException(
                    it, point.dim(it), shape.dim(it)
                )
        }
        return (0 until point.ndim).sumOf { point.dim(it) * blockSize[it] }
    }

    private fun indexToPoint(index: Int, drop: Int): Point {
        val dims = IntArray(ndim - drop)
        var index = index
        (0 until ndim - drop).forEach {
            dims[it] = index / blockSize[it]
            index %= blockSize[it]
        }
        return DefaultPoint(*dims)
    }

    override fun at(point: Point): Int = data[pointToIndex(point)]

    override fun set(point: Point, value: Int) {
        data[pointToIndex(point)] = value
    }

    override fun copy(): NDArray = DefaultNDArray(data.clone(), shape)

    override fun view(): NDArray = object : NDArray by this {}

    override fun add(other: NDArray) {
        if (other.ndim != ndim && other.ndim != ndim - 1) {
            throw NDArrayException.IllegalDimensionException(ndim, other.ndim)
        }
        val diff = ndim - other.ndim
        (0 until ndim - diff).forEach {
            if (dim(it) != other.dim(it))
                throw NDArrayException.IllegalDimensionException(
                    dim(it), other.dim(it)
                )
        }
        (0 until size).forEach { data[it] += other.at(indexToPoint(it, diff)) }
    }

    override fun dot(other: NDArray): NDArray {
        if (ndim != 2) {
            throw NDArrayException.IllegalDimensionException(2, ndim)
        }
        if (other.ndim > 2) {
            throw NDArrayException.IllegalDimensionException(2, other.ndim)
        }
        if (other.ndim == 1) {
            if (dim(1) != other.dim(0)) {
                throw NDArrayException.IllegalDimensionException(dim(1), other.dim(0))
            }
            val result = (0 until dim(0)).map { i ->
                (0 until dim(1)).sumOf { j ->
                    at(DefaultPoint(i, j)) * other.at(DefaultPoint(j))
                }
            }
            return DefaultNDArray(result.toIntArray(), DefaultShape(dim(0)))
        }
        val result = zeros(DefaultShape(dim(0), other.dim(1)))
        (0 until dim(0)).forEach { i ->
            (0 until other.dim(1)).forEach { j ->
                result.set(DefaultPoint(i, j),
                    (0 until dim(1)).sumOf { k ->
                        at(DefaultPoint(i, k)) * other.at(DefaultPoint(k, j))
                    })
            }
        }
        return result
    }

    override val size: Int = shape.size
    override val ndim: Int = shape.ndim
    override fun unsafeDim(i: Int): Int = shape.unsafeDim(i)

    companion object {
        fun zeros(shape: Shape): DefaultNDArray = DefaultNDArray(IntArray(shape.size) { 0 }, shape)

        fun ones(shape: Shape): DefaultNDArray = DefaultNDArray(IntArray(shape.size) { 1 }, shape)
    }

}

sealed class NDArrayException(reason: String) : IllegalArgumentException(reason) {
    class IllegalPointCoordinateException(dim: Int, index: Int, bound: Int) :
        NDArrayException("Incorrect index at dim $dim: index $index is out of bound [0..$bound]")

    class IllegalPointDimensionException(expected: Int, found: Int) :
        NDArrayException("Expected $expected, found $found")

    class IllegalDimensionException(expected: Int, found: Int) :
        NDArrayException("Expected $expected, found $found")
}