plugins {
    `java-library`
}

dependencies {
    implementation(project(":files"))
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.api)
    implementation(libs.assertj.core)
}
