
interface Point: DimentionAware

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(private vararg val parametrs: Int) : Point {
    private val coordinates = ArrayList<Int>()
    init {
        for (par : Int in parametrs) {
            coordinates.add(par)
        }
    }

    override val ndim: Int
        get() = coordinates.size

    override fun dim(i: Int): Int = coordinates[i]
}