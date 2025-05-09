package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat

import kotlin.test.Test

class ConsecutiveCombinatorTest {
    // Character parsers
    private val a: Parsikle<Char> = parse('A')
    private val b: Parsikle<Char> = parse('B')
    private val c: Parsikle<Char> = parse('C')
    private val d: Parsikle<Char> = parse('D')
    private val e: Parsikle<Char> = parse('E')
    private val f: Parsikle<Char> = parse('F')
    private val g: Parsikle<Char> = parse('G')
    private val h: Parsikle<Char> = parse('H')

    @Test
    fun `consecutive 3 - success yields Triple`() {
        assertThat(consecutive(a, b, c))
            .whenParses("ABC")
            .succeeds()
            .withValue(Triple('A', 'B', 'C'))
    }

    @Test
    fun `consecutive 3 - failure on third parser`() {
        assertThat(consecutive(a, b, c))
            .whenParses("ABX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'C'"))
    }

    @Test
    fun `consecutive 4 - success yields Tuple4`() {
        assertThat(consecutive(a, b, c, d))
            .whenParses("ABCD")
            .succeeds()
            .withValue(Tuple4('A', 'B', 'C', 'D'))
    }

    @Test
    fun `consecutive 4 - failure on fourth parser`() {
        assertThat(consecutive(a, b, c, d))
            .whenParses("ABCX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'D'"))
    }

    @Test
    fun `consecutive 5 - success yields Tuple5`() {
        assertThat(consecutive(a, b, c, d, e))
            .whenParses("ABCDE")
            .succeeds()
            .withValue(Tuple5('A', 'B', 'C', 'D', 'E'))
    }

    @Test
    fun `consecutive 5 - failure on fifth parser`() {
        assertThat(consecutive(a, b, c, d, e))
            .whenParses("ABCDX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'E'"))
    }

    @Test
    fun `consecutive 6 - success yields Tuple6`() {
        assertThat(consecutive(a, b, c, d, e, f))
            .whenParses("ABCDEF")
            .succeeds()
            .withValue(Tuple6('A', 'B', 'C', 'D', 'E', 'F'))
    }

    @Test
    fun `consecutive 6 - failure on sixth parser`() {
        assertThat(consecutive(a, b, c, d, e, f))
            .whenParses("ABCDEX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'F'"))
    }

    @Test
    fun `consecutive 7 - success yields Tuple7`() {
        assertThat(consecutive(a, b, c, d, e, f, g))
            .whenParses("ABCDEFG")
            .succeeds()
            .withValue(Tuple7('A', 'B', 'C', 'D', 'E', 'F', 'G'))
    }

    @Test
    fun `consecutive 7 - failure on seventh parser`() {
        assertThat(consecutive(a, b, c, d, e, f, g))
            .whenParses("ABCDEFX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'G'"))
    }

    @Test
    fun `consecutive 8 - success yields Tuple8`() {
        assertThat(consecutive(a, b, c, d, e, f, g, h))
            .whenParses("ABCDEFGH")
            .succeeds()
            .withValue(Tuple8('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'))
    }

    @Test
    fun `consecutive 8 - failure on eighth parser`() {
        assertThat(consecutive(a, b, c, d, e, f, g, h))
            .whenParses("ABCDEFGX")
            .fails<SimpleError>()
            .withError(SimpleError("Expected 'H'"))
    }
}