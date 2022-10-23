
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
class DefaultShape(private vararg val dimentions: Int): Shape {
    override val ndim = dimentions.size
    override fun dim(i: Int): Int = dimentions[i]
    override val size: Int

    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }

        var sizeCount = 1
        for (i in dimentions.indices) {
            if (dimentions[i] <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(i, dimentions[i])
            }
            sizeCount *= dimentions[i]
        }
        size = sizeCount
    }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("Exception: Empty Shape.")
    class NonPositiveDimensionException(index: Int, value: Int) :
        ShapeArgumentException("Exception: Non-Positive Dimension at $index : $value.")
}
