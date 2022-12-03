
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
        if (dimensions.isEmpty()) throw ShapeArgumentException.EmptyShapeException()
        when(val value = dimensions.find(fun(a: Int) = (a <= 0))) {
            is Int -> throw ShapeArgumentException.NonPositiveDimensionException(dimensions.indexOf(value), value)
        }
    }
    override val ndim: Int
        get() = dimensions.size

    override fun dim(i: Int): Int = dimensions[i]

    override val size: Int
        get() = dimensions.fold(1, fun (x, y) = x * y)
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Expected not empty dimension")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Found not positive dimension by index $index with value $value")
}
