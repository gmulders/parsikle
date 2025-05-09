package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class MapCombinatorTest {

    @Test
    fun `map transforms a successful result`() {
        assertThat(digit map { c -> c.digitToInt() })
            .whenParses("7abc")
            .succeeds()
            .withValue(7)
            .at(1, 2)
    }

    @Test
    fun `invoke operator is alias for map`() {
        assertThat(digit { c -> c.digitToInt() })
            .whenParses("7abc")
            .succeeds()
            .withValue(7)
            .at(1, 2)
    }

    @Test
    fun `mapError transforms parser errors`() {
        assertThat(parse('x') mapError { e -> SimpleError("Mapped: ${e.message}") })
            .whenParses("y")
            .fails<SimpleError>()
            .withError(SimpleError("Mapped: Expected 'x'"))
            .at(1, 1)
    }
}
