package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class PrimitiveParserTest {
    private val letters: Parsikle<String> = parseWhile("letter", Char::isLetter)
    private val digits: Parsikle<String> = parseWhile("digit", Char::isDigit)

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

    @Test
    fun `parseWhile consumes all matching characters`() {
        assertThat(letters)
            .whenParses("abcXYZ123")
            .succeeds()
            .withValue("abcXYZ")
            .at(1, 7)
    }

    @Test
    fun `parseWhile returns empty on no initial match`() {
        assertThat(letters)
            .whenParses("123abc")
            .succeeds()
            .withValue("")
            .at(1, 1)
    }

    @Test
    fun `parseWhile works for digit predicate`() {
        assertThat(digits)
            .whenParses("4567!")
            .succeeds()
            .withValue("4567")
            .at(1, 5)
    }

    @Test
    fun `parseWhile can parse entire string`() {
        assertThat(letters)
            .whenParses("hello")
            .succeeds()
            .withValue("hello")
            .at(1, 6)
    }

    @Test
    fun `digit parses single digit and advances state`() {
        assertThat(digit)
            .whenParses("7a")
            .succeeds()
            .withValue('7')
            .at(1, 2)
    }

    @Test
    fun `digit fails on non-digit`() {
        assertThat(digit)
            .whenParses("x1")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }

    @Test
    fun `number parses multi-digit numbers`() {
        assertThat(number)
            .whenParses("12345z")
            .succeeds()
            .withValue(12345)
            .at(1, 6)
    }

    @Test
    fun `number parses single-digit`() {
        assertThat(number)
            .whenParses("9!")
            .succeeds()
            .withValue(9)
            .at(1, 2)
    }

    @Test
    fun `number fails on leading zero`() {
        assertThat(number)
            .whenParses("0abc")
            .fails<SimpleError>()
            .withError(SimpleError("Number must not start with a '0'"))
            .at(1, 1)
    }
}
