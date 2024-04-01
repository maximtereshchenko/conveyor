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
        relocate("com.fasterxml.jackson.annotation", "com.github.maximtereshchenko.conveyor.jackson.annotation")
        excludes.remove("module-info.class")
        dependencies {
            exclude {
                !(it.module.toString().startsWith(libs.jackson.dataformat.xml.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.databind.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.core.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.core.get().toString()) ||
                        it.module.toString().startsWith(libs.jackson.annotations.get().toString())
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
