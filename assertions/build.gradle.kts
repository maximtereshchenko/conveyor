plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter.params)
    api(libs.assertj.core)
}
