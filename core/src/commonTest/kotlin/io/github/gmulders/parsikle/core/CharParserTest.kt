package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class CharParserTest {

    @Test
    fun `validate 'a' parser`() {
        val parser = parse('a')

        assertThat(parser)
            .whenParses("b")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'a'"))
            .at(1, 1)

        assertThat(parser)
            .whenParses("")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'a'"))
            .at(1, 1)

        assertThat(parser)
            .whenParses("a")
            .succeeds()
            .withNoRemainder()
            .at(1, 2)

        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withRemainder("b")
            .at(1, 2)
    }
}
