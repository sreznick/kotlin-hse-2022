interface Point : DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint constructor(private vararg val coordinate: Int) : Point {
    override val ndim: Int
        get() = coordinate.size

    override fun dim(i: Int): Int = coordinate[i]
}