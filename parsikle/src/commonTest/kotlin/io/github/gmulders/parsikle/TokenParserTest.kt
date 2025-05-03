package io.github.gmulders.parsikle

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class TokenParserTest {

    @Test
    fun `validate '^a' parser`() {
        val regex = Regex("^a")
        val parser = tokenParser(regex, Unit)

        assertThat(parser)
            .whenParses("b")
            .fails<RegularExpressionError>()
            .withError(RegularExpressionError(regex, "b"))
            .at(1, 1)

        assertThat(parser)
            .whenParses("")
            .fails<RegularExpressionError>()
            .withError(RegularExpressionError(regex, ""))
            .at(1, 1)

        assertThat(parser)
            .whenParses("a")
            .succeeds()
            .withValue("Expected matchValue 'a'") { it.matchValue == "a" }
            .withNoRemainder()
            .at(1, 2)

        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withValue("Expected matchValue 'a'") { it.matchValue == "a" }
            .withRemainder("b")
            .at(1, 2)
    }

    @Test
    fun `validate 'b' parser`() {
        val regex = Regex("b")
        val parser = tokenParser(regex, Unit)

        assertThat(parser)
            .whenParses("a")
            .fails<RegularExpressionError>()
            .withError(RegularExpressionError(regex, "a"))
            .at(1, 1)

        assertThat(parser)
            .whenParses("")
            .fails<RegularExpressionError>()
            .withError(RegularExpressionError(regex, ""))
            .at(1, 1)

        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withValue("Expected matchValue 'b'") { it.matchValue == "b" }
            .withNoRemainder()
            .at(1, 3)

        assertThat(parser)
            .whenParses("abc")
            .succeeds()
            .withValue("Expected matchValue 'b'") { it.matchValue == "b" }
            .withRemainder("c")
            .at(1, 3)
    }
}
