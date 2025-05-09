package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class ManyCombinatorsTest {
    private val comma: Parsikle<Char> = parse("Expected comma") { it == ',' }

    @Test
    fun `many parses multiple matches and stops on first non-match`() {
        assertThat(digit.many())
            .whenParses("123x")
            .succeeds()
            .withValue(listOf('1', '2', '3'))
            .at(1, 4)
    }

    @Test
    fun `many succeeds with empty list on no initial match`() {
        assertThat(digit.many())
            .whenParses("xyz")
            .succeeds()
            .withValue(emptyList())
            .at(1, 1)
    }

    @Test
    fun `manyUntil parses until terminator and consumes terminator`() {
        assertThat(digit.manyUntil(comma))
            .whenParses("456,rest")
            .succeeds()
            .withValue(listOf('4', '5', '6'))
            .at(1, 5)
    }

    @Test
    fun `manyUntil returns empty list when terminator at start`() {
        assertThat(digit.manyUntil(comma))
            .whenParses(",abc")
            .succeeds()
            .withValue(emptyList())
            .at(1, 2)
    }

    @Test
    fun `manyUntil fails if no terminator found`() {
        assertThat(digit.manyUntil(comma))
            .whenParses("78")
            .fails<OrError>()
            .withError(
                OrError(
                    OrError(
                        OrError(
                            SimpleError("Expected digit"),
                            SimpleError("Expected comma")
                        ),
                        SimpleError("Expected comma"),
                    ),
                    SimpleError("Expected comma"),
                )
            )
            .at(1, 1)
    }

    @Test
    fun `many1 parses at least one match`() {
        assertThat(digit.many1())
            .whenParses("9x")
            .succeeds()
            .withValue(listOf('9'))
            .at(1, 2)
    }

    @Test
    fun `many1 fails when no match`() {
        assertThat(digit.many1())
            .whenParses("y")
            .fails<SimpleError>()
            .withError(SimpleError("Expected digit"))
            .at(1, 1)
    }
}