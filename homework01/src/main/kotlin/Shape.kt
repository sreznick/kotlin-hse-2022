
interface Shape: DimentionAware, SizeAware

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
class DefaultShape constructor(private vararg val dimensions: Int): Shape {

    init {
        if (dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for ((pos, dim) in dimensions.withIndex()) {
            if (dim <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(pos, dim)
            }
        }
    }

    override val ndim: Int
        get() = dimensions.size

    override fun dim(i: Int) = dimensions[i]

    override val size = dimensions.reduce { mlt, next -> mlt * next }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    // EmptyShapeException
    // NonPositiveDimensionException(val index: Int, val value: Int)
    class EmptyShapeException(): ShapeArgumentException("Cannot create an array with zero dimensions")
    class NonPositiveDimensionException(index: Int, value: Int): ShapeArgumentException(
        "Dimension $index is $value, but non-positive value expected"
    )
}
