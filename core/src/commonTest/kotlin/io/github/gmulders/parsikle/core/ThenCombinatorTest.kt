package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class ThenCombinatorTest {
    private val a: Parsikle<Char> = parse('a')
        .withContext("a")
    private val b: Parsikle<Char> = parse('b')
        .withContext("\"b context\"")

    @Test
    fun `then success returns correct pair and clears context`() {
        val parser = a then b
        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withValue('a' to 'b')
            .at(1, 3)
    }

    @Test
    fun `then failure preserves b context`() {
        val parser = a then b
        assertThat(parser)
            .whenParses("ac")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'b'"))
            .at(1, 2)
            .withContext(Context(1, "\"b context\""))
    }

    @Test
    fun `then then failure preserves c context`() {
        val c = parse('c').withContext("c")
        val parser = a then (b then c).withContext("second")
        assertThat(parser)
            .whenParses("abd")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'c'"))
            .at(1, 3)
            .withContext(Context(1, "second"), Context(2, "c"))
    }

    @Test
    fun `ignoreThen returns second parser's result on success`() {
        val parser = a ignoreThen b
        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withValue('b')
            .at(1, 3)
    }

    @Test
    fun `ignoreThen fails and rewinds on second parser failure`() {
        val parser = a ignoreThen b
        assertThat(parser)
            .whenParses("ac")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'b'"))
            .at(1, 2)
            .withContext(Context(1, "\"b context\""))
    }

    @Test
    fun `thenIgnore returns first parser's result on success`() {
        val parser = a thenIgnore b
        assertThat(parser)
            .whenParses("ab")
            .succeeds()
            .withValue('a')
            .at(1, 3)
    }

    @Test
    fun `thenIgnore fails and rewinds on second parser failure`() {
        val parser = a thenIgnore b
        assertThat(parser)
            .whenParses("ac")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'b'"))
            .at(1, 2)
            .withContext(Context(1, "\"b context\""))
    }
}
