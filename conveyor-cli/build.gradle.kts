plugins {
    application
}

dependencies {
    implementation(project(":conveyor-core"))
    implementation(project(":jackson-adapter"))
}

tasks.register<Jar>("standaloneJar") {
    group = "build"
    description = "Build a JAR with all runtime dependencies"
    manifest {
        attributes["Main-Class"] = "com.github.maximtereshchenko.conveyor.cli.Main"
    }
    archiveClassifier.set("standalone")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { zipTree(it) })
}
