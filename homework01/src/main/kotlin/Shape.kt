
interface Shape: DimensionAware, SizeAware {
    enum class PointLocation {
        INSIDE, OUTSIDE, WRONG_DIM
    }
    /*
    * Если точка невалидна - соответствующее исключение
    * Иначе true или false - лежит ли точка внутри
    * */
    fun innerPoint(point: Point): PointLocation {
        if (point.dimNumber != dimNumber) {
            return PointLocation.WRONG_DIM
        }
        return if ((0 until dimNumber).all { point.dim(it) in 0 until dim(it) }) PointLocation.INSIDE
                                                                       else PointLocation.OUTSIDE
    }
}

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultShape(10), DefaultShape(12, 3), DefaultShape(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * При попытке создать пустой Shape бросается EmptyShapeException
 *
 * При попытке указать неположительное число по любой размерности бросается NonPositiveDimensionException
 * Свойство index - минимальный индекс с некорректным значением, value - само значение
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultShape(private vararg val dimensions: Int): Shape {
    init {
        if (dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        dimensions.forEachIndexed { index, dim ->
            if (dim <= 0) throw ShapeArgumentException.NonPositiveDimensionException(index + 1, dim) }
    }

    override val dimNumber = dimensions.size

    // 0 <= i < n
    override fun dim(i: Int) = dimensions[i]

    override val size = dimensions.reduce { sz, curDim -> sz * curDim }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException: ShapeArgumentException("Empty shape is restricted")

    class NonPositiveDimensionException(index: Int, value: Int):
        ShapeArgumentException("Expected positive number as ${index}th argument, but got $value")
}
