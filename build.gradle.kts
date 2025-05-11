plugins {
    base
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    id("org.jetbrains.dokka") version "2.0.0"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

allprojects {
    group = "io.github.gmulders.parsikle"
    version = "0.0.4"
}