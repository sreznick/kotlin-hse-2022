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
class DefaultShape(private vararg val dimentions: Int) : Shape {
    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        for ((i, value) in dimentions.withIndex()) {
            if (value < 1) {
                throw ShapeArgumentException.NonPositiveDimensionException(i, value)
            }
        }
    }

    override val ndim: Int
        get() = dimentions.size

    override fun dim(i: Int): Int {
        return dimentions[i]
    }

    override val size: Int
        get() = dimentions.reduce { acc, x -> acc * x }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException()
    class NonPositiveDimensionException(val index: Int, val value: Int) :
        ShapeArgumentException("Incorrect value $value at index $index")
}
