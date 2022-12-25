interface Point : DimensionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(private vararg val coordinates: Int) : Point {
    init {
        if (ndim == 0) {
            throw PointArgumentException.EmptyPointException()
        }
        for (coordinate in coordinates.withIndex()) {
            if (coordinate.value < 0) {
                throw PointArgumentException.NegativeCoordinateException(coordinate.index, coordinate.value)
            }
        }
    }

    override val ndim: Int
        get() = coordinates.size

    override fun dim(i: Int): Int {
        if (i < 0 || i >= ndim) {
            throw PointArgumentException.IllegalPointCoordinateIndexException(ndim, i)
        }
        return coordinates[i]
    }

}

sealed class PointArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyPointException : PointArgumentException()

    class NegativeCoordinateException(index: Int, value: Int) : ShapeArgumentException("$index coordinate is $value")

    class IllegalPointCoordinateIndexException(ndim: Int, index: Int) :
        ShapeArgumentException("Point's ndim is $ndim, calling coordinate index is $index")
}
