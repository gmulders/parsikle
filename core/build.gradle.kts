import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

kotlin {
    jvm()
    macosArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.arrow.core)
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

    coordinates(group.toString(), "core", version.toString())

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
