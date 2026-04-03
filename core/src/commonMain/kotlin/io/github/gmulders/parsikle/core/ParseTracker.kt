package io.github.gmulders.parsikle.core

/**
 * Tracks the furthest position reached during parsing.
 * Shared by reference across all [ParserState] copies from the same parse run.
 */
class ParseTracker {
    var furthestIndex: Int = 0
        private set

    fun track(index: Int) {
        if (index > furthestIndex) furthestIndex = index
    }
}
