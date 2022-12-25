interface Shape : DimentionAware, SizeAware

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
    override val ndim: Int = dimensions.size
    override fun dim(i: Int): Int = dimensions[i]
    override val size: Int
        get() = dimensions.reduce { acc, i ->  acc * i }

    init {
        if (dimensions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        dimensions.withIndex().map {
            if (it.value <= 0) throw ShapeArgumentException.NonPositiveDimensionException(it.index, it.value)
        }
    }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Shape is empty")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Value at index $index is non-positive: $value")
}

fun main() {
    val a = DefaultShape()
}