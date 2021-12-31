// Build a Kotlin application
plugins {
    kotlin("jvm") version "1.6.10"
    application
}

// Use Maven Central as our repository
repositories {
    mavenCentral()
}

// Compile for JDK 8 to match the Docker container
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
// Set where to look for source code
sourceSets.main {
    java.srcDirs("src/", "codebase/")
}

// Set the main class
application {
    mainClass.set("MainKt")
}
