package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class WhitespaceParserTest {

    @Test
    fun `whiteSpace parses multiple whitespace`() {
        assertThat(whiteSpace)
            .whenParses(" \t \n")
            .succeeds()
            .withValue(" \t \n")
            .at(2, 1)
    }

    @Test
    fun `whiteSpace returns empty on no whitespace`() {
        assertThat(whiteSpace)
            .whenParses("abc")
            .succeeds()
            .withValue("")
            .at(1, 1)
    }
}
