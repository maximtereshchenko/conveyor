plugins {
    `java-library`
    `test-conventions`
}

dependencies {
    api(project(":conveyor-api"))
    api(project(":conveyor-plugin-api"))
    implementation(project(":files"))
    testImplementation(project(":compiler"))
    testImplementation(project(":zip-archive"))
    testImplementation(project(":jackson-adapter"))
    testImplementation(project(":assertions"))
    testImplementation(platform(libs.jackson.bom))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)

    testImplementation(libs.wiremock)
    testImplementation(libs.slf4j.jdk14)
    testImplementation(libs.jackson.dataformat.xml)
}
