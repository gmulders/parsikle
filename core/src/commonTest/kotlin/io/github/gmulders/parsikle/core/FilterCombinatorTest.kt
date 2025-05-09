package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class FilterCombinatorTest {
    private val nonZero: Parsikle<Char> =
        digit.filter("Digit must not be zero") { it != '0' }

    @Test
    fun `filter allows values passing predicate`() {
        assertThat(nonZero)
            .whenParses("7x")
            .succeeds()
            .withValue('7')
            .at(1, 2)
    }

    @Test
    fun `filter fails and resets index on predicate failure`() {
        assertThat(nonZero)
            .whenParses("0a")
            .fails<SimpleError>()
            .withError(SimpleError("Digit must not be zero"))
            .at(1, 1)
    }

    @Test
    fun `filter preserves context on failure`() {
        assertThat(digit.withContext("digit").filter("Not zero") { it != '0' }.withContext("non zero digit"))
            .whenParses("0")
            .fails<SimpleError>()
            .withError(SimpleError("Not zero"))
            .withContext(Context(0, "non zero digit"))
            .at(1, 1)
    }
}