package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class ContextCombinatorTest {
    private val digitParser: Parsikle<Int> =
        digit.map { it.code - '0'.code }
            .withContext("digit")

    private val plus: Parsikle<Char> =
        parse("Expected '+'") { it == '+' }

    private val sumParser: Parsikle<Pair<Int, Int>> =
        (digitParser thenIgnore plus then digitParser)
            .withContext("sum expr")

    @Test
    fun `parse 2+5 succeeds and context is empty`() {
        assertThat(sumParser)
            .whenParses("2+5")
            .succeeds()
            .withEmptyContext()
    }

    @Test
    fun `parse 2-5 fails with sum expr context only`() {
        assertThat(sumParser)
            .whenParses("2-5")
            .fails<SimpleError>()
            .withContext(Context(0, "sum expr"))
    }

    @Test
    fun `parse 2+a fails with both contexts`() {
        assertThat(sumParser)
            .whenParses("2+a")
            .fails<SimpleError>()
            .withContext(Context(0, "sum expr"), Context(2, "digit"))
    }
}