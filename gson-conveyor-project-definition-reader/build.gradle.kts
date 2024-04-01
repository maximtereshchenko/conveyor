plugins {
    `java-library`
}

dependencies {
    api(project(":conveyor-api"))
    implementation(libs.gson)
}
