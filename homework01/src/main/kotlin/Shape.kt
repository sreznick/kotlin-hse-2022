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
class DefaultShape(private vararg val dimensions: Int): Shape {
    init {
        if (dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for (i in dimensions.indices) {
            if (dimensions[i] < 1) {
                throw ShapeArgumentException.NonPositiveDimensionException(i, dimensions[i])
            }
        }
    }
    override val ndim = dimensions.size
    override fun dim(i: Int):Int = dimensions[i]

    override val size: Int = dimensions.reduce { acc, x -> acc * x }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    // EmptyShapeException
    // NonPositiveDimensionException(val index: Int, val value: Int)
    class EmptyShapeException() : ShapeArgumentException()
    class NonPositiveDimensionException(val index: Int, val value: Int) : ShapeArgumentException()
}
