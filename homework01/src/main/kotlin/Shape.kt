interface Shape : DimensionAware, SizeAware

/**
 * Реализация Shape по умолчаению
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
class DefaultShape(vararg dimensions: Int) : DefaultDimensionAware(dimensions), Shape {

    init {
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException()
        dimensions.forEachIndexed { index, value ->
            if (value <= 0) throw ShapeArgumentException.NonPositiveDimensionException(index, value)
        }
    }

    override val size: Int
        get() = dimensions.reduce { acc, it -> acc * it }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Empty shape is not allowed")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("On $index of shape is non-positive value $value")
}
