package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class EndParserTest {

    @Test
    fun `end succeeds on empty input`() {
        assertThat(end)
            .whenParses("")
            .succeeds()
            .at(1, 1)
    }

    @Test
    fun `end succeeds at actual end of non-empty input`() {
        assertThat(parse('a') thenIgnore end)
            .whenParses("a")
            .succeeds()
            .withValue('a')
            .at(1, 2)
    }

    @Test
    fun `end fails when input remains`() {
        assertThat(end)
            .whenParses("a")
            .fails<SimpleError>()
            .withError(SimpleError("Could not match end"))
            .at(1, 1)
    }
}
