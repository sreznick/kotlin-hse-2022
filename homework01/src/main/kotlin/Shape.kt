interface Shape : DimensionAware, SizeAware

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
class DefaultShape(private vararg val dimensions: Int) : Shape {
    private var shapeSize = 1

    init {
        if (ndim == 0) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for (dimension in dimensions.withIndex()) {
            if (dimension.value <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(dimension.index, dimension.value)
            }
            shapeSize *= dimension.value
        }
    }

    override val ndim: Int
        get() = dimensions.size

    override fun dim(i: Int): Int {
        if (i < 0 || i >= ndim) {
            throw ShapeArgumentException.IllegalShapeDimensionIndexException(ndim, i)
        }
        return dimensions[i]
    }

    override val size: Int
        get() = shapeSize

}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    // EmptyShapeException
    // NonPositiveDimensionException(val index: Int, val value: Int)
    class EmptyShapeException : ShapeArgumentException()

    class NonPositiveDimensionException(index: Int, value: Int) : ShapeArgumentException("$index dimension is $value")

    class IllegalShapeDimensionIndexException(ndim: Int, index: Int) :
        ShapeArgumentException("Shape's ndim is $ndim, calling dimension index is $index")
}
