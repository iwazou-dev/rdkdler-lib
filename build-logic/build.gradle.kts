plugins {
    `kotlin-dsl`
}

dependencies {
    // https://plugins.gradle.org/plugin/com.diffplug.spotless
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.1.0")
    // https://plugins.gradle.org/plugin/io.freefair.lombok
    implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:9.1.0")
}
