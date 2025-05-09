package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test
import kotlin.test.assertEquals

class LazyCombinatorTest {
    private var invocationCount = 0

    // Base parser that increments a counter and succeeds
    private val baseParser: Parsikle<String> = { state ->
        invocationCount++
        Success("ok", state)
    }

    // Deferred parser via lazy
    private val lazyParser: Parsikle<String> = lazy { baseParser }

    @Test
    fun `lazy defers parser creation until first invocation`() {
        // Before any parse, baseParser should not have been invoked
        assertEquals(0, invocationCount)

        assertThat(lazyParser)
            .whenParses("")
            .succeeds()

        assertEquals(1, invocationCount, "Base parser should be invoked once")

        assertThat(lazyParser)
            .whenParses("")
            .succeeds()

        assertEquals(2, invocationCount, "Base parser should be invoked twice")
    }

    @Test
    fun `lazy supports recursive definitions`() {
        // A parser for 'a' optionally followed by more 'a's
        val aParser: Parsikle<Char> = parse('a')
        fun manyA(): Parsikle<List<Char>> = lazy {
            (aParser then manyA()).map { (c, rest) -> listOf(c) + rest } or
                    succeed(emptyList())
        }

        assertThat(manyA())
            .whenParses("aa")
            .succeeds()
            .withValue(listOf('a', 'a'))
            .at(1, 3)
    }
}