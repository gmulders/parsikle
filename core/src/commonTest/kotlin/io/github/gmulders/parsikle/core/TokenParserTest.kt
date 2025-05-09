package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class TokenParserTest {
    enum class Token { NUMBER, WORD }
    private val numberRegex = Regex("^\\d+")
    private val numberToken: Parsikle<TokenMatch<Token>> =
        tokenParser(numberRegex, Token.NUMBER)

    private val wordToken: Parsikle<TokenMatch<Token>> =
        tokenParser(Regex("[a-zA-Z]+"), Token.WORD)

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

    @Test
    fun `tokenParser successfully matches digits and consumes correctly`() {
        assertThat(numberToken)
            .whenParses("4567xyz")
            .succeeds()
            .withValue("Expected matchValue '4567'") { it.matchValue == "4567"  }
            .withValue("Expected type NUMBER") { it.type == Token.NUMBER }
            .at(1, 5)
    }

    @Test
    fun `tokenParser successfully matches letters and leaves remainder`() {
        assertThat(wordToken)
            .whenParses("hello123")
            .succeeds()
            .withValue("Expected matchValue 'hello'") { it.matchValue == "hello"  }
            .withValue("Expected type WORD") { it.type == Token.WORD }
            .at(1, 6)
    }

    @Test
    fun `tokenParser fails without consuming on no match`() {
        assertThat(numberToken)
            .whenParses("!?123")
            .fails<RegularExpressionError>()
            .withError(RegularExpressionError(numberRegex, "!?123"))
            .at(1, 1)
    }
}
