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
// We set both Java and Kotlin to the same thing. We only use Kotlin so it
//  should be fine.
sourceSets.main {
    java.srcDirs("src/", "src/")
}

// Set the main class
application {
    mainClass.set("MainKt")
}
