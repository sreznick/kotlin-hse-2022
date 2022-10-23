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
class DefaultShape(private vararg val dimensions: Int) : Shape {
    init {
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException()
        dimensions.forEachIndexed { index, value ->
            if (value < 0) throw ShapeArgumentException.NonPositiveDimensionException(
                index,
                value
            )
        }
    }

    override val ndim: Int = dimensions.size

    override fun unsafeDim(i: Int): Int = dimensions[i]

    override val size: Int = dimensions.reduce { acc, i -> acc * i }
}

sealed class ShapeArgumentException(reason: String) : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Empty shape")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Non positive dimension $value at index $index")
}
