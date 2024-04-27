plugins {
    `conveyor-component`
}

dependencies {
    api(project(":conveyor-api"))
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.databind)
}
