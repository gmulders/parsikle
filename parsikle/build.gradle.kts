import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.gmulders"
version = "0.0.1"

kotlin {
    jvm()
    macosArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.arrow-kt:arrow-core:2.0.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(project(":parsikle-test"))
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "parsikle", version.toString())

    pom {
        name = "Parsikle"
        description = "Lightweight but powerful parser combinator library"
        inceptionYear = "2025"
        url = "https://github.com/gmulders/parsikle/"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/license/MIT"
            }
        }
        developers {
            developer {
                id = "gmulders"
                url = "https://github.com/gmulders/"
            }
        }
        scm {
            url = "https://github.com/gmulders/parsikle.git"
        }
    }
}
