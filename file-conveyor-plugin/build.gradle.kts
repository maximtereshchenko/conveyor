plugins {
    `java-library`
}

dependencies {
    api(project(":conveyor-plugin-api"))
}

tasks.jar {
    doLast {
        copy {
            from("${project.layout.buildDirectory.get()}/libs")
            into("${project.parent?.layout?.projectDirectory}/conveyor-domain/src/test/resources/test-project/repository")
            rename("${project.name}-${project.version}.jar", "${project.name}-1.jar")
        }
    }
}
