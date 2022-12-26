
interface Shape: DimensionAware, SizeAware

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
        if(dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        val badIndex = dimensions.indexOfFirst { it <= 0 }
        if(badIndex != -1) {
            throw ShapeArgumentException.NonPositiveDimensionException(
                badIndex, dimensions[badIndex])
        }
    }
    override val ndim = dimensions.size

    override fun dim(i: Int) = dimensions[i]

    override val size = dimensions.reduce { acc, i -> acc * i }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
      class EmptyShapeException : ShapeArgumentException("Empty Shape")
    data class NonPositiveDimensionException(val index: Int, val value: Int)
        : ShapeArgumentException ("Illegal dimension at index $index : $value")
}
