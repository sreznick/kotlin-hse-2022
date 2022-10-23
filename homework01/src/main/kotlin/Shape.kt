
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
    override val ndim: Int = this.dimentions.size

    override fun dim(i: Int): Int = this.dimentions[i]

    override val size: Int

    init {
        if (this.dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        var countedSize = 1
        for ((index, value) in this.dimentions.withIndex()) {
            if (value <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, value)
            }
            countedSize *= value
        }
        this.size = countedSize
    }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException: ShapeArgumentException("No arguments")
    class NonPositiveDimensionException(val index: Int, val value: Int):
        ShapeArgumentException("Non positive value: $value found on position ${index + 1}")
}
