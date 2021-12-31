// Build a Kotlin application
plugins {
    kotlin("jvm") version "1.6.10"
    application
}

// Use Maven Central as our repository
repositories {
    mavenCentral()
}

// Depend on Log4J
// Use a vulnerable version
dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
    implementation("org.apache.logging.log4j:log4j-core:2.14.0")
}

// Compile for JDK 8 to match the Docker container
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
// Set where to look for source code and resources
sourceSets.main {
    java.srcDirs("src/")
    resources.srcDirs("res/")
}

// Set the main class
application {
    mainClass.set("MainKt")
}
