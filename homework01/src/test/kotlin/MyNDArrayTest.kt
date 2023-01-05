import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class MyNDArrayTest {

    @Test
    fun testDot() {
        val data = DefaultNDArray.ones(DefaultShape(2, 2))
        val data2 = DefaultNDArray.ones(DefaultShape(2, 2))
        val data3 = DefaultNDArray.ones(DefaultShape(2))

        data.set(DefaultPoint(0, 0), 2)
        data.set(DefaultPoint(0, 1), 3)
        data2.set(DefaultPoint(1, 1), 4)
        data2.set(DefaultPoint(1, 0), 2)
        data3.set(DefaultPoint(0), 1)
        data3.set(DefaultPoint(1), 2)

        val result = data.dot(data2) // TEST 1
        val result2 = data.dot(data3) // TEST 2

        // TEST 1
        assertEquals(8, result.at(DefaultPoint(0, 0)))
        assertEquals(14, result.at(DefaultPoint(0, 1)))
        assertEquals(3, result.at(DefaultPoint(1, 0)))
        assertEquals(5, result.at(DefaultPoint(1, 1)))

        // TEST 2
        assertEquals(8, result2.at(DefaultPoint(0, 0)))
        assertEquals(3, result2.at(DefaultPoint(1, 0)))
    }
}
