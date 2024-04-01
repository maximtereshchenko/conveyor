plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.asm)
    implementation(libs.asm.commons)
    implementation(libs.shadow)
}
