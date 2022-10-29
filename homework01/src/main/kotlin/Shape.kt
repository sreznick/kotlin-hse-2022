
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
    override val ndim: Int = dimentions.size

    override fun dim(i: Int): Int = dimentions[i]

    override val size: Int
    
    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException()
        }
        var countMulti = 1
        for ((index, value) in dimentions.withIndex()) {
            if (value <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, value)
            }
            countMulti *= value
        }
        size = countMulti
    }

    override fun toString(): String = buildString {
        append("Shape:")
        dimentions.map { append(" $it") }
    }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    // EmptyShapeException
    // NonPositiveDimensionException(val index: Int, val value: Int)

    class EmptyShapeException: ShapeArgumentException("Invalid empty shape")
    class NonPositiveDimensionException(index: Int, value: Int): ShapeArgumentException("Invalid dimension: value=$value, index=$index")
}
