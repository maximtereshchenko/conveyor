plugins {
    `java-library`
}

tasks.jar {
    doLast {
        val from = "${project.layout.buildDirectory.get()}/libs"
        val into =
            "${project.parent?.layout?.projectDirectory}/conveyor-domain/src/test/resources/project-with-plugins-sharing-common-dependency/repository"
        val file = "${project.name}-${project.version}.jar"
        copy {
            from(from)
            into(into)
            rename(file, "${project.name}-1.jar")
        }
        copy {
            from(from)
            into(into)
            rename(file, "${project.name}-2.jar")
        }
    }
}
