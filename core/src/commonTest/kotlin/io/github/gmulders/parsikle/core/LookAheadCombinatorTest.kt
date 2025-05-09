package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class LookAheadCombinatorTest {
    private val letter: Parsikle<Char> = parse("Expected letter") { it.isLetter() }

    @Test
    fun `lookAhead succeeds without consuming on success`() {
        assertThat(digit.lookAhead())
            .whenParses("7a")
            .succeeds()
            .withValue('7')
            .at(1, 1)
    }

    @Test
    fun `lookAhead fails without consuming on failure`() {
        assertThat(letter.lookAhead())
            .whenParses("7a")
            .fails<SimpleError>()
            .withError(SimpleError("Expected letter"))
            .at(1, 1)
    }

    @Test
    fun `chained lookAhead then actual parse consumes only once`() {
        assertThat(digit.lookAhead() then digit)
            .whenParses("7a")
            .succeeds()
            .withValue('7' to '7')
            .at(1, 2)
    }
}
