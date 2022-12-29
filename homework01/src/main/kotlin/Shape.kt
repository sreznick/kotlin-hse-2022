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
class DefaultShape(private vararg val dims: Int) : Shape {
    override val ndim: Int
        get() = dims.size

    override val size: Int = dims.reduce { acc, element -> acc * element }

    override fun dim(i: Int): Int = dims[i]

    init {
        if (dims.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for ((index, dim) in dims.withIndex()) {
            if (dim <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, dim)
            }
        }
    }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException: ShapeArgumentException("shape can't be empty")
    class NonPositiveDimensionException(index: Int, dim: Int): ShapeArgumentException(
        "dimensions can't be non positive: dimension with index ${index + 1} is equals to $dim"
    )
}
