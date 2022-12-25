
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
    override val ndim: Int
        get() = dimentions.size

    override var size: Int = 0

    override fun dim(i: Int): Int {
        return dimentions.getOrNull(i) ?: throw ShapeArgumentException.NonPositiveDimensionException(i, null);
    }

    init {
        if (dimentions.isEmpty()) {
            throw ShapeArgumentException.EmptyShapeException();
        }

        size = dimentions.reduce { accumulator, elem -> accumulator * elem }

        for ((index, value) in dimentions.withIndex()) {
            if (value <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, value);
            }
        }
    }
}

sealed class ShapeArgumentException (reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException() : ShapeArgumentException("Cannot create empty shape")
    class NonPositiveDimensionException(val index: Int, val value: Int?) : ShapeArgumentException("Incorrect index or value: <$index, $value>")
}