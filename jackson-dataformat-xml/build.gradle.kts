plugins {
    java
    `shadow-conventions`
    `conveyor-component`
}

shadowConventions {
    libraries = setOf(
        libs.jackson.dataformat.xml,
        libs.jackson.databind,
        libs.jackson.core,
        libs.jackson.annotations
    )
        .map { it.get().toString() }
    packages = setOf(
        "com.fasterxml.jackson.dataformat.xml",
        "com.fasterxml.jackson.databind",
        "com.fasterxml.jackson.core",
        "com.fasterxml.jackson.annotation"
    )
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.dataformat.xml)
}
