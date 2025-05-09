package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class OneOfCombinatorTest {
    // Define some simple parsers
    private val letter: Parsikle<Char> = parse("Expected letter") { it.isLetter() }
    private val star: Parsikle<Char> = parse('*')

    // oneOf combining three alternatives
    private val anyOfThree: Parsikle<Char> = oneOf(digit, letter, star)

    @Test
    fun `oneOf returns first parser result when it matches`() {
        assertThat(anyOfThree)
            .whenParses("7")
            .succeeds()
            .withValue('7')
            .at(1, 2)
    }

    @Test
    fun `oneOf returns second parser result when first fails`() {
        assertThat(anyOfThree)
            .whenParses("x")
            .succeeds()
            .withValue('x')
            .at(1, 2)
    }

    @Test
    fun `oneOf returns third parser result when first two fail`() {
        assertThat(anyOfThree)
            .whenParses("*")
            .succeeds()
            .withValue('*')
            .at(1, 2)
    }

    @Test
    fun `oneOf fails with combined OrError when all parsers fail`() {
        assertThat(anyOfThree)
            .whenParses("#")
            .fails<OrError>()
            .withError(
                OrError(
                    OrError(
                        SimpleError("Expected digit"),
                        SimpleError("Expected letter"),
                    ),
                    SimpleError("Expected '*'"),
                )
            )
            .at(1, 1)
    }
}