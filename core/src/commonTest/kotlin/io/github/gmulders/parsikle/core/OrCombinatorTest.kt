package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class OrCombinatorTest {
    private val a: Parsikle<Char> = parse("Expected 'a'") { it == 'a' }
    private val b: Parsikle<Char> = parse("Expected 'b'") { it == 'b' }
    private val eitherAB: Parsikle<Char> = a or b

    @Test
    fun `or returns first parser's result when it succeeds`() {
        assertThat(eitherAB)
            .whenParses("a")
            .succeeds()
            .withValue('a')
            .at(1, 2)
    }

    @Test
    fun `or returns second parser's result when first fails`() {
        assertThat(eitherAB)
            .whenParses("b")
            .succeeds()
            .withValue('b')
            .at(1, 2)
    }

    @Test
    fun `or fails with OrError when both parsers fail`() {
        assertThat(eitherAB)
            .whenParses("c")
            .fails<OrError>()
            .withError(OrError(SimpleError("Expected 'a'"), SimpleError("Expected 'b'")))
            .at(1, 1)
    }
}
