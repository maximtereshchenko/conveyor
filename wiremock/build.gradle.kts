plugins {
    java
    `shadow-conventions`
    `conveyor-component`
}

shadowConventions {
    libraries = setOf(libs.wiremock.get().toString(), "com.github.jknack:handlebars")
    packages = setOf("com.github.tomakehurst.wiremock", "com.github.jknack.handlebars")
}

dependencies {
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.wiremock)
}
