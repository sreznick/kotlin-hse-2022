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
    override val ndim: Int
        get() = dimensions.size

    override var size: Int = 0

    init {
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException()

        size = dimensions.reduce { accumulator, element ->
            accumulator * element
        }

        val firstIncorrectIndex = dimensions.indexOfFirst { it <= 0 }
        if (firstIncorrectIndex != -1) throw ShapeArgumentException.NonPositiveDimensionException(
            firstIncorrectIndex,
            dimensions[firstIncorrectIndex]
        )
    }

    override fun dim(i: Int): Int = dimensions.getOrNull(i) ?: throw ShapeArgumentException.NonPositiveDimensionException(i, null)
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException() : ShapeArgumentException("Shape must have at least 1 dimension")

    class NonPositiveDimensionException(val index: Int, val value: Int?) :
        ShapeArgumentException(if (value == null) "Incorrect index = $index of dimension" else "Dimension with index = $index has incorrect value = $value")
}
