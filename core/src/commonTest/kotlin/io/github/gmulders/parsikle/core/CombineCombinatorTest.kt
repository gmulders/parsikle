package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test
import io.github.gmulders.parsikle.core.digit as digitChar

class CombineCombinatorTest {
    private val doubleFn: (Int) -> Int = { x: Int -> x * 2 }
    private val doubleNumberParser: Parsikle<Int> = succeed(doubleFn) combine number

    private val sumFn: (Int) -> (Int) -> Int = { x: Int -> { y: Int -> x + y } }
    private val digit = digitChar.map { it.code - '0'.code }
    private val sumParser: Parsikle<Int> = digit.combine(digit, sumFn)

    @Test
    fun `when number parser succeeds then combine correctly applies function to result`() {
        assertThat(doubleNumberParser)
            .whenParses("37")
            .succeeds()
            .withValue(74)
            .at(1, 3)
    }

    @Test
    fun `when first parser fails then combine fails with the first error`() {
        val failFn: Parsikle<(Int) -> Int> =
            parse("Expected 'f'") { it == 'f' }
                .map { _ -> { n: Int -> n } }

        assertThat(failFn combine number)
            .whenParses("37")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'f'"))
            .at(1, 1)
    }

    @Test
    fun `when second parser fails then combine fails with the second error`() {
        assertThat(doubleNumberParser)
            .whenParses("x")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }

    @Test
    fun `when both parsers succeed then combine succeeds and combines values correctly`() {
        assertThat(sumParser)
            .whenParses("37")
            .succeeds()
            .withValue(10)
            .at(1, 3)
    }

    @Test
    fun `when first parser fails then combine fails`() {
        assertThat(sumParser)
            .whenParses("a5")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }

    @Test
    fun `when second parser fails then combine fails`() {
        assertThat(sumParser)
            .whenParses("5a")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 2)
    }
}