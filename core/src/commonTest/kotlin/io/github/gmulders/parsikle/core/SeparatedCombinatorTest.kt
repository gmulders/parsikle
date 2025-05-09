package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class SeparatedCombinatorsTest {
    private val comma: Parsikle<Char> = parse("Expected comma") { it == ',' }

    @Test
    fun `separatedBy parses multiple values`() {
        assertThat(digit.separatedBy(comma))
            .whenParses("1,2,3")
            .succeeds()
            .withValue(listOf('1','2','3'))
            .at(1, 6)
    }

    @Test
    fun `separatedBy parses single value`() {
        assertThat(digit.separatedBy(comma))
            .whenParses("7")
            .succeeds()
            .withValue(listOf('7'))
            .at(1, 2)
    }

    @Test
    fun `separatedBy returns empty list on no match`() {
        assertThat(digit.separatedBy(comma))
            .whenParses("")
            .succeeds()
            .withValue(emptyList())
            .at(1, 1)
    }

    @Test
    fun `separatedBy succeeds when only separator present`() {
        assertThat(digit.separatedBy(comma))
            .whenParses(",1")
            .succeeds()
            .withValue(emptyList())
            .at(1, 1)
    }

    @Test
    fun `separated parses values and separators`() {
        assertThat(separated(digit, comma))
            .whenParses("1,2,3")
            .succeeds()
            .withValue(listOf('1', '2', '3') to listOf(',', ','))
            .at(1, 6)
    }

    @Test
    fun `separated fails when first element missing`() {
        assertThat(separated(digit, comma))
            .whenParses("")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }
}