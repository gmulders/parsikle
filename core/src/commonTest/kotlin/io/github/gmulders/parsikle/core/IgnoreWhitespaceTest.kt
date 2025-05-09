package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class IgnoreWhitespaceTest {

    @Test
    fun `ignoreWhitespace succeeds after leading spaces`() {
        assertThat(ignoreWhitespace(digit))
            .whenParses("    7abc")
            .succeeds()
            .withValue('7')
            .at(1, 6)
    }

    @Test
    fun `ignoreWhitespace succeeds with no leading spaces`() {
        assertThat(ignoreWhitespace(digit))
            .whenParses("7abc")
            .succeeds()
            .withValue('7')
            .at(1, 2)
    }

    @Test
    fun `ignoreWhitespace fails and consumes only whitespace on parser failure`() {
        assertThat(ignoreWhitespace(digit))
            .whenParses("   x")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 4)
    }
}