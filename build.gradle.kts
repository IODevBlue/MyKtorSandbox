plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.plugin.serialization)
}

// group = "com.blueiobase.api.cloud.ktor"
group = "io.github.iodevblue.sandbox.ktor.playground"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.kotest.property)
    implementation(libs.kotest.assertions.core)
    implementation(libs.kotest.runner.junit5)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.logback.classic)
    implementation(libs.org.jsoup)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

}
