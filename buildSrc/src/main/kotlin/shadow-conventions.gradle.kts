plugins {
    java
    id("com.github.johnrengelman.shadow")
}

abstract class ShadowConventionsExtension @Inject constructor(objectFactory: ObjectFactory) {

    val libraries = objectFactory.setProperty(String::class.java)
    val packages = objectFactory.setProperty(String::class.java)
}

val extension = extensions.create<ShadowConventionsExtension>("shadowConventions")

tasks {
    shadowJar {
        excludes.remove("module-info.class")
        archiveClassifier = null
        dependencies {
            exclude { extension.libraries.get().none { allowed -> it.module.toString().startsWith(allowed) } }
        }
        extension.packages.get().forEach { relocate(it, "$it.shadowed") }
    }
    jar {
        enabled = false
        dependsOn(shadowJar)
    }
}
