package io.github.gmulders.parsikle.core

import io.github.gmulders.parsikle.test.assertThat
import kotlin.test.Test

class SucceedFailParserTest {

    @Test
    fun `succeed always returns the given value without consuming input`() {
        assertThat(succeed(37))
            .whenParses("input")
            .succeeds()
            .withValue(37)
            .at(1, 1)
    }

    data class CustomError(val isCustom: Boolean = true) : Error {
        override val message: String = "This is a custom error"
    }

    @Test
    fun `fail always fails with the given error without consuming input`() {
        assertThat(fail<Int>(CustomError()))
            .whenParses("input")
            .fails<CustomError>()
            .withError(CustomError())
            .at(1, 1)
    }

    @Test
    fun `fail always fails with the given error message without consuming input`() {
        assertThat(fail<Int>("Some message"))
            .whenParses("input")
            .fails<SimpleError>()
            .withError(SimpleError("Some message"))
            .at(1, 1)
    }
}
