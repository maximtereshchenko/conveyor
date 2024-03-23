plugins {
    java
    alias(libs.plugins.shadow)
}

buildscript {
    dependencies {
        classpath(libs.asm)
        classpath(libs.asm.commons)
    }
}

tasks {
    shadowJar {
        relocate("com.github.tomakehurst.wiremock", "com.github.maximtereshchenko.conveyor.wiremock")
        excludes.remove("module-info.class")
        dependencies {
            exclude {
                listOf("org.wiremock", "com.github.jknack")
                    .none { allowed -> it.moduleGroup.startsWith(allowed) }
            }
        }
        archiveClassifier = null
    }
    jar {
        enabled = false
        dependsOn(shadowJar)
    }
}

dependencies {
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.wiremock)
}
