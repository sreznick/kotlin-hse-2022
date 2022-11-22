
interface Shape: DimensionAware, SizeAware

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
class DefaultShape(private vararg val dimensions: Int): Shape {
    init {
        if (dimensions.isEmpty())
            throw ShapeArgumentException.EmptyShapeException()
    }

    override val ndim: Int = dimensions.size
    override val size: Int = dimensions.foldIndexed(1) { ind, acc, i ->
        if (i <= 0)
            throw ShapeArgumentException.NonPositiveDimensionException(ind, i)
        acc * i
    }

    override fun dim(i: Int): Int = dimensions[i]
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Can't create empty shape")

    class NonPositiveDimensionException(index: Int, value: Int)
        : ShapeArgumentException("Dimension must be positive, found $value at position $index")
}
