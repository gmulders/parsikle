package io.github.gmulders.parsikle.core

typealias Parsikle<R> = (ParserState) -> Result<Error, R>

data class SimpleError(override val message: String) : Error
