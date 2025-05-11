# Getting Started

Parsikle is available on Maven Central and designed to work seamlessly in Kotlin Multiplatform projects.

### Install via Gradle

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.gmulders.parsikle:core:0.0.4")
}
```

For Kotlin Multiplatform setups, include both core and test modules under the appropriate source sets:

```kotlin
kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation("io.github.gmulders.parsikle:core:0.0.4")
      }
    }
    commonTest {
      dependencies {
        implementation("io.github.gmulders.parsikle:parsikle-test:0.0.4")
      }
    }
  }
}
```

With these dependencies configured, you’re ready to start defining parsers and composing grammars in a functional style.
Feel free to explore the core concepts and combinators in the following sections.
