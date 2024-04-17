import java.nio.file.Files
import java.nio.file.Path

plugins {
    `java-library`
}

tasks.withType(Jar::class.java) {
    tasks.register<Copy>("installConveyorComponent") {
        group = "distribution"
        description = "Install the Conveyor component to a local Conveyor directory"
        from(archiveFile)
        val directory = project.group.toString()
            .split(".")
            .fold(project.rootDir.toPath().resolve(".conveyor-repository"), Path::resolve)
            .resolve(project.name)
            .resolve(project.version.toString())
        into(directory)
        dependsOn(this@withType)
        doLast {
            val dependencies = configurations
                .compileClasspath
                .get()
                .resolvedConfiguration
                .firstLevelModuleDependencies
                .filter { !it.moduleName.contains("bom") }
                .joinToString(",") {
                    println(it)
                    """
                    {
                        "group": "${it.moduleGroup}",
                        "name": "${it.moduleName}",
                        "version": "${it.moduleVersion}"
                    }
                    """.trimIndent()
                }
            Files.writeString(
                directory.resolve("${project.name}-${project.version}.json"),
                """
                {
                    "group": "${project.group}",
                    "name": "${project.name}",
                    "version": "${project.version}",
                    "dependencies": [ $dependencies ]
                }
                """.trimIndent()
            )
        }
    }
}

