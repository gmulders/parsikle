# Combinators

Parsikle offers a comprehensive set of combinators to build powerful parsers from small primitives. Below are key combinators with explanations:

| Combinator                         | Description                                                                                  |
|------------------------------------|----------------------------------------------------------------------------------------------|
| `then`                             | Run one parser, then another on the remaining input, returning a `Pair` of results.          |
| `ignoreThen` / `thenIgnore`        | Variants of `then` that discard one of the pair results (useful for skipping delimiters).    |
| `between`                          | Parse an opening delimiter, a body, and a closing delimiter, returning only the body.        |
| `or`                               | Try the first parser; if it fails, backtrack and try the second, merging errors on failure.  |
| `oneOf`                            | Chain multiple alternatives with `or`, picking the first successful parse.                   |
| `map`                              | Transform a successful parse result into a new value.                                        |
| `mapError`                         | Transform or wrap parser errors.                                                             |
| `succeed`                          | Always succeed with a given value without consuming input.                                   |
| `fail`                             | Always fail with a given error without consuming input.                                      |
| `combine`                          | Applicative style: parse a function then an argument, applying the function to the value.    |
| `lazy`                             | Defer parser creation, enabling recursive grammar definitions.                               |
| `many` / `many1` / `manyUntil`     | Parse zero-or-more (or one-or-more) repetitions, collecting results into a `List`.           |
| `separatedBy`                      | Parse a list of items separated by a delimiter parser (e.g. comma-separated values).         |
| `separated`                        | Like `separatedBy`, but also returns the parsed separators alongside the values.             |
| `leftAssociate` / `rightAssociate` | Fold lists of terms and separators into left- or right-associative operations.               |
| `chainLeft` / `chainRight`         | Chain binary operator parsers with left- or right-associative folding.                       |
| `lookAhead`                        | Peek at upcoming input without consuming it.                                                 |
| `filter`                           | Apply an additional predicate to a successful parse, failing if it doesn't hold.             |
| `optional`                         | Make a parser optional, returning `null` on failure instead of failing.                      |
| `slice`                            | Capture the exact substring consumed by a parser.                                            |
| `lexeme`                           | Skip surrounding whitespace before and after a parser.                                       |
| `spanned`                          | Wrap a parser's result in a `Spanned` that captures the source region it was parsed from.    |
| `withContext`                      | Annotate a parser with a named context frame for improved error messages.                    |
| `consecutive`                      | Sequence 3-8 parsers, collecting results into a `Triple` or `TupleN`.                        |
| `parse`                            | Primitive for single characters by predicate or exact match.                                 |
| `parseWhile`                       | Parse zero-or-more characters matching a predicate into a `String`.                          |
| `tokenParser`                      | Regex-based token parser that matches and advances by the regex match length.                |
