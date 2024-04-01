dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val asm = version("asm", "9.6")
            library("asm", "org.ow2.asm", "asm").versionRef(asm)
            library("asm-commons", "org.ow2.asm", "asm-commons").versionRef(asm)
            library("shadow", "com.github.johnrengelman", "shadow").version("8.1.1")
        }
    }
}
