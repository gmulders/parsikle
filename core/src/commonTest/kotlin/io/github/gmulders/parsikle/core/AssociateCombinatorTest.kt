package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class AssociateCombinatorTest {
    // Parser for '-' separators
    private val minusSign: Parsikle<Char> = parse('-')

    // Subtraction as left-associative: (a - b) - c
    private val leftSub: Parsikle<Int> =
        leftAssociate(number, minusSign) { acc, _, term -> acc - term }

    // Subtraction as right-associative: a - (b - c)
    private val rightSub: Parsikle<Int> =
        rightAssociate(number, minusSign) { acc, _, term -> acc - term }

    // Parser for '-' producing subtraction function
    private val subOp: Parsikle<(Int, Int) -> Int> =
        parse('-')
            .map { _ ->
                { a: Int, b: Int ->
                    a - b
                }
            }

    private val chainLeft = number.chainLeft(subOp)
    private val chainRight = number.chainRight(subOp)

    @Test
    fun `leftAssociate subtracts left-to-right`() {
        assertThat(leftSub)
            .whenParses("47-3-2")
            .succeeds()
            .withValue(42)
            .at(1, 7)
    }

    @Test
    fun `rightAssociate subtracts right-to-left`() {
        assertThat(rightSub)
            .whenParses("43-3-2")
            .succeeds()
            .withValue(42)
            .at(1, 7)
    }

    @Test
    fun `associate with single term returns that term`() {
        assertThat(leftSub)
            .whenParses("37")
            .succeeds()
            .withValue(37)
            .at(1, 3)

        assertThat(rightSub)
            .whenParses("37")
            .succeeds()
            .withValue(37)
            .at(1, 3)
    }

    @Test
    fun `chainLeft subtracts left-to-right`() {
        assertThat(chainLeft)
            .whenParses("42-3-2")
            .succeeds()
            .withValue(37)
            .at(1, 7)
    }

    @Test
    fun `chainRight subtracts right-to-left`() {
        assertThat(chainRight)
            .whenParses("38-3-2")
            .succeeds()
            .withValue(37)
            .at(1, 7)
    }

    @Test
    fun `chainLeft with single term returns the term`() {
        assertThat(chainLeft)
            .whenParses("37")
            .succeeds()
            .withValue(37)
            .at(1, 3)
    }

    @Test
    fun `chainRight with single term returns the term`() {
        assertThat(chainLeft)
            .whenParses("37")
            .succeeds()
            .withValue(37)
            .at(1, 3)
    }
}
