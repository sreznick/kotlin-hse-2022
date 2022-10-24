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
        dimentions.forEachIndexed { index, i ->
            if (i <= 0) {
                throw ShapeArgumentException.NonPositiveDimensionException(index, i)
            }
        }
    }

    override val ndim = dimentions.size

    override fun dim(i: Int) = dimentions[i]

    override val size: Int by lazy {
        return@lazy dimentions.fold(1) { a, b -> a * b }
    }
}

sealed class ShapeArgumentException(reason: String = "") : IllegalArgumentException(reason) {
    class EmptyShapeException : ShapeArgumentException("")
    class NonPositiveDimensionException(val index: Int, val value: Int) :
        ShapeArgumentException("index = ${index}, value = ${value}")
}
