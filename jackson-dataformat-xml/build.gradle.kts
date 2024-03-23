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
        relocate("com.fasterxml.jackson.dataformat.xml", "com.github.maximtereshchenko.conveyor.jackson.dataformat.xml")
        relocate("com.fasterxml.jackson.databind", "com.github.maximtereshchenko.conveyor.jackson.databind")
        relocate("com.fasterxml.jackson.core", "com.github.maximtereshchenko.conveyor.jackson.core")
        excludes.remove("module-info.class")
        dependencies {
            exclude {
                !(it.module.toString().startsWith(libs.jackson.dataformat.xml.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.databind.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.core.get().toString())
                        )
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
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.dataformat.xml)
}
