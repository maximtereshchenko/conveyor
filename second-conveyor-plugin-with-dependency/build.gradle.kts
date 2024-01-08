plugins {
    `java-library`
}

dependencies {
    api(project(":conveyor-plugin-api"))
    implementation(project(":conveyor-plugin-dependency"))
}

tasks.jar {
    doLast {
        copy {
            from("${project.layout.buildDirectory.get()}/libs")
            into("${project.parent?.layout?.projectDirectory}/conveyor-domain/src/test/resources/project-with-plugins-sharing-common-dependency/repository")
            rename("${project.name}-${project.version}.jar", "${project.name}-1.jar")
        }
    }
}
