package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseTrackerTest {

    private val a: Parsikle<Char> = parse('a')
    private val b: Parsikle<Char> = parse('b')
    private val c: Parsikle<Char> = parse('c')

    @Test
    fun `tracker records furthest position on success`() {
        assertThat(a thenIgnore b thenIgnore c)
            .whenParses("abc")
            .succeeds()
            .withFurthestIndex(3)
    }

    @Test
    fun `tracker records furthest position on backtracking failure`() {
        val deep = a thenIgnore b thenIgnore c thenIgnore parse('d')
        val shallow = a thenIgnore parse('Y')
        assertThat(deep or shallow)
            .whenParses("abcZ")
            .fails<OrError>()
            .withFurthestIndex(3)
    }

    @Test
    fun `tracker survives deep backtracking through oneOf`() {
        val alt1 = a thenIgnore b thenIgnore c thenIgnore parse('d') thenIgnore parse('e')
        val alt2 = a thenIgnore b thenIgnore parse('X')
        val alt3 = a thenIgnore parse('Y')
        assertThat(oneOf(alt1, alt2, alt3))
            .whenParses("abcdZ")
            .fails<OrError>()
            .withFurthestIndex(4)
    }

    @Test
    fun `tracker is shared across state copies`() {
        val state = ParserState("abcdef")
        state.next(2)
        state.next(4)
        assertEquals(4, state.tracker.furthestIndex)
    }

    @Test
    fun `tracker starts at zero`() {
        val state = ParserState("abc")
        assertEquals(0, state.tracker.furthestIndex)
    }
}
