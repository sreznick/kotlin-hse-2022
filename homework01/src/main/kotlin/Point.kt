
interface Point: DimensionAware {
    override fun cutPrefix(prefixLen: Int): Point {
        return object : Point {
            override val dimNumber = this@Point.dimNumber - prefixLen

            override fun dim(i: Int): Int = this@Point.dim(i) // UB if i > dimNumber
        }
    }
}

/**
 * Реализация Point по умолчаению
 *
 * Должны работать вызовы DefaultPoint(10), DefaultPoint(12, 3), DefaultPoint(12, 3, 12, 4, 56)
 * с любым количество параметров
 *
 * Сама коллекция параметров недоступна, доступ - через методы интерфейса
 */
class DefaultPoint(private vararg val coordinates: Int): Point {

    override val dimNumber = coordinates.size

    // 0 <= i < dimNumber
    override fun dim(i: Int): Int {
        return if (i !in 0 until dimNumber)
                    throw IllegalDimensionException("Dimension $i does not fit range 1..$dimNumber")
               else coordinates[i]
    }

    override fun toString(): String {
        return coordinates.toString()
    }

    class IllegalDimensionException(reason: String): IllegalArgumentException(reason)
}