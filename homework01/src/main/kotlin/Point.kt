interface Point : DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(vararg args: Int) : Point {
    private val pointCoordinates: IntArray = args

    override val ndim: Int
        get() = pointCoordinates.size

    override fun dim(i: Int): Int {
        if (i <= ndim) return pointCoordinates[i] else throw IndexOutOfBoundsException()
    }
}