package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class SpannedCombinatorTest {
    private val digit: Parsikle<Char> = parse("Expected digit") { it.isDigit() }

    @Test
    fun `spanned captures start and end indices`() {
        assertThat(number.spanned())
            .whenParses("42abc")
            .succeeds()
            .withValue("Expected value 42") { it.value == 42 }
            .withValue("Expected span 0..2") { it.span.start == 0 && it.span.end == 2 }
            .withValue("Expected text '42'") { it.span.text == "42" }
            .at(1, 3)
            .withRemainder("abc")
    }

    @Test
    fun `spanned preserves parser state after match`() {
        assertThat(number.spanned())
            .whenParses("123rest")
            .succeeds()
            .withValue("Expected value 123") { it.value == 123 }
            .at(1, 4)
            .withRemainder("rest")
    }

    @Test
    fun `spanned propagates failure`() {
        assertThat(number.spanned())
            .whenParses("abc")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }

    @Test
    fun `spanned captures correct span after preceding input`() {
        assertThat(parse('(') ignoreThen number.spanned() thenIgnore parse(')'))
            .whenParses("(99)")
            .succeeds()
            .withValue("Expected value 99") { it.value == 99 }
            .withValue("Expected span 1..3") { it.span.start == 1 && it.span.end == 3 }
            .withNoRemainder()
    }

    @Test
    fun `spanned reports correct line and column`() {
        assertThat(parseWhile("ws") { it.isWhitespace() } ignoreThen number.spanned())
            .whenParses("   42")
            .succeeds()
            .withValue("Expected span line 1, column 4") { it.span.line == 1 && it.span.column == 4 }
            .withNoRemainder()
    }

    @Test
    fun `spanned works with multiline input`() {
        assertThat(parseWhile("prefix") { it != '\n' } ignoreThen parse('\n') ignoreThen number.spanned())
            .whenParses("hello\n789")
            .succeeds()
            .withValue("Expected span line 2, column 1") { it.span.line == 2 && it.span.column == 1 }
            .withValue("Expected span 6..9") { it.span.start == 6 && it.span.end == 9 }
            .withNoRemainder()
    }

    @Test
    fun `spanned composes with map`() {
        data class NumLit(val value: Int, val span: Span)

        assertThat(number.spanned().map { NumLit(it.value, it.span) })
            .whenParses("256rest")
            .succeeds()
            .withValue("Expected value 256") { it.value == 256 }
            .withValue("Expected span 0..3") { it.span.start == 0 && it.span.end == 3 }
            .at(1, 4)
            .withRemainder("rest")
    }

    @Test
    fun `spanned on single char parser`() {
        assertThat(digit.spanned())
            .whenParses("7x")
            .succeeds()
            .withValue("Expected value '7'") { it.value == '7' }
            .withValue("Expected span 0..1") { it.span.start == 0 && it.span.end == 1 }
            .at(1, 2)
            .withRemainder("x")
    }
}