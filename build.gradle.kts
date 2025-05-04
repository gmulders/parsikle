plugins {
    base
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

allprojects {
    group = "io.github.gmulders.parsikle"
    version = "0.0.3"
}