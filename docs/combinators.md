# Combinators

Parsikle offers a comprehensive set of combinators to build powerful parsers from small primitives. Below are key combinators with explanations:

| Combinator                         | Description                                                                                 |
|------------------------------------| ------------------------------------------------------------------------------------------- |
| `then`                             | Run one parser, then another on the remaining input, returning a `Pair` of results.         |
| `ignoreThen` / `thenIgnore`        | Variants of `then` that discard one of the pair results (useful for skipping delimiters).   |
| `or`                               | Try the first parser; if it fails, backtrack and try the second, merging errors on failure. |
| `oneOf`                            | Chain multiple alternatives with `or`, picking the first successful parse.                  |
| `map`                              | Transform a successful parse result into a new value.                                       |
| `mapError`                         | Transform or wrap parser errors.                                                            |
| `combine`                          | Applicative style: parse a function then an argument, applying the function to the value.   |
| `lazy`                             | Defer parser creation, enabling recursive grammar definitions.                              |
| `many` / `many1` / `manyUntil`     | Parse zero-or-more (or one-or-more) repetitions, collecting results into a `List`.          |
| `separatedBy`                      | Parse a list of items separated by a delimiter parser (e.g. comma-separated values).        |
| `leftAssociate` / `rightAssociate` | Fold lists of terms and separators into left- or right-associative operations.              |
| `lookAhead`                        | Peek at upcoming input without consuming it.                                                |
| `parse`                            | Primitive for single characters by predicate or exact match.                                |
| `tokenParser`                      | Regex-based token parser that matches and advances by the regex match length.               |

