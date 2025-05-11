package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class MiscCombinatorsTest {
    private val digit: Parsikle<Char> = parse("Expected digit") { it.isDigit() }

    @Test
    fun `optional succeeds with value when parser matches`() {
        assertThat(digit.optional())
            .whenParses("5x")
            .succeeds()
            .withValue('5')
            .at(1, 2)
            .withRemainder("x")
    }

    @Test
    fun `optional succeeds with null when parser fails`() {
        assertThat(digit.optional())
            .whenParses("xyz")
            .succeeds()
            .withValue(null)
            .at(1, 1)
            .withRemainder("xyz")
    }

    @Test
    fun `slice captures exact consumed text`() {
        assertThat(number.slice())
            .whenParses("213a")
            .succeeds()
            .withValue("213")
            .at(1, 4)
            .withRemainder("a")
    }

    @Test
    fun `slice propagates failure without change`() {
        assertThat(fail<Unit>("oops").slice())
            .whenParses("anything")
            .fails<SimpleError>()
            .withError(SimpleError("oops"))
            .at(1, 1)
    }

    @Test
    fun `lexeme trims surrounding whitespace before and after`() {
        assertThat(number.lexeme())
            .whenParses("   37   +")
            .succeeds()
            .withValue(37)
            .at(1, 9)
            .withRemainder("+")
    }

    @Test
    fun `lexeme propagates failure after initial whitespace`() {
        assertThat(number.lexeme())
            .whenParses("   b   ")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 4)
    }
}
