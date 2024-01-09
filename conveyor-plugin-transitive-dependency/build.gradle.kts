plugins {
    `java-library`
}

tasks.jar {
    doLast {
        copy {
            from("${project.layout.buildDirectory.get()}/libs")
            into("${project.parent?.layout?.projectDirectory}/conveyor-domain/src/test/resources/project-with-plugin-transitive-dependency/repository")
            rename("${project.name}-${project.version}.jar", "${project.name}-1.jar")
        }
    }
}
