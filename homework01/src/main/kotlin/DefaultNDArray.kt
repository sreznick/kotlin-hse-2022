import java.lang.RuntimeException

interface NDArray: SizeAware, DimensionAware {
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
     * Аналогично, если размерность this - (10, 5, 5), а размерность other - (10, 5), то мы для пять раз прибавим
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
class DefaultNDArray private constructor(private val shape: Shape, filler: (Int) -> Int) : NDArray {
    override val dimNumber = shape.dimNumber

    override fun dim(i: Int) = shape.dim(i)

    override val size = shape.size

    private val data = IntArray(size) { index -> filler(index) }
    override fun at(point: Point): Int {
        return when (shape.innerPoint(point)) {
            Shape.PointLocation.INSIDE -> data[shape.pointToIndex(point)]
            Shape.PointLocation.OUTSIDE -> throw NDArrayException.IllegalPointCoordinateException(point, shape)
            Shape.PointLocation.WRONG_DIM -> throw NDArrayException.IllegalPointDimensionException(point, shape)
        }
    }

    override fun set(point: Point, value: Int) {
        when (shape.innerPoint(point)) {
            Shape.PointLocation.INSIDE -> data[shape.pointToIndex(point)] = value
            Shape.PointLocation.OUTSIDE -> throw NDArrayException.IllegalPointCoordinateException(point, shape)
            Shape.PointLocation.WRONG_DIM -> throw NDArrayException.IllegalPointDimensionException(point, shape)
        }
    }

    override fun copy(): NDArray {
        return DefaultNDArray(shape) { i -> data[i] }
    }

    override fun view(): NDArray {
        return object : NDArray by this {}
    }

    override fun add(other: NDArray) {
        var type = 2
        // type - количество координат, которые нужно откинуть у точки данного массива.
        // Если > 1, то складывать нельзя
        type = if (equalDimensions(other)) 0 else type
        type = if (other.extendedDimensions(this, 1)) 1 else type
        if (type <= 1) {
            (0 until size).forEach { data[it] += other.at(shape.indexToPoint(it).cutPrefix(type)) }
        } else {
            throw NDArrayException.IllegalAddArgument(shape, other)
        }
    }

    override fun dot(other: NDArray): NDArray {
        if (dimNumber != 2) {
            throw NDArrayException.IllegalDotArguments("this dimension is $dimNumber != 2")
        } else if (other.dimNumber > 2) {
            throw NDArrayException.IllegalDotArguments("dot argument dimension is ${other.dimNumber} != 2")
        } else if (other.dim(0) != dim(1)) {
            throw NDArrayException.IllegalDotArguments("Second this dimension != other's first dimension")
        } else {
            val resultShape = DefaultShape(dim(0), if (other.dimNumber == 2) other.dim(1) else 1)
            val ptProducer: (Int, Int) -> Point = if (other.dimNumber == 2) { i, j -> DefaultPoint(i, j) }
                                                  else { i, _ -> DefaultPoint(i) }
            return DefaultNDArray(resultShape) {
                val pt = resultShape.indexToPoint(it)
                val i = pt.dim(0)
                val j = pt.dim(1)
                (0 until dim(1)).sumOf { k -> at(DefaultPoint(i, k)) * other.at(ptProducer(k, j)) }
            }
        }
    }

    companion object ArrayProducer {
        fun ones(shape: Shape) = DefaultNDArray(shape) { 1 }

        fun zeros(shape: Shape) = DefaultNDArray(shape) { 0 }
    }
}

fun Shape.pointToIndex(point: Point): Int {
    return (1 until dimNumber).fold(point.dim(0)) { cnt, d ->
        cnt * dim(d) + point.dim(d) }
}

fun Shape.indexToPoint(index: Int): Point {
    val result = IntArray(dimNumber)
    var cntInd = index
    for (i in dimNumber - 1 downTo 0) {
        result[i] = cntInd % dim(i)
        cntInd /= dim(i)
    }
    return DefaultPoint(*result)
}

sealed class NDArrayException(reason: String): RuntimeException(reason) {
    /* TODO: реализовать требуемые исключения */
    class IllegalPointCoordinateException(pt: Point, shp: Shape):
        NDArrayException("Point $pt is not in shape $shp")
    class IllegalPointDimensionException(pt: Point, shp: Shape):
        NDArrayException("Point has dimension ${pt.dimNumber}, while shape has dimension ${shp.dimNumber}")

    class IllegalAddArgument(myShape: DimensionAware, addShape: DimensionAware):
        NDArrayException("Array of shape $addShape cannot be added to array of shape $myShape")

    class IllegalDotArguments(reason: String): NDArrayException(reason)
}