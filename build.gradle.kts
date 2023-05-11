group = "com.example"

allprojects {
    repositories {
        mavenCentral()
    }
    version = "0.0.1"
}

object Versions {
    const val JUNIT = "5.9.2"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.21"

    id("maven-publish")
    id("com.jfrog.artifactory") version "4.31.7"
    id("idea")
    id("org.springframework.boot") version "3.0.1"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
}

apply(plugin = "java")

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.21.9")

    // Spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")

    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.awaitility:awaitility:4.2.0")

    testImplementation("org.testcontainers:testcontainers:1.18.1")
    testImplementation("org.testcontainers:junit-jupiter:1.18.1")
    testImplementation("org.testcontainers:kafka:1.18.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
}

tasks.apply {
    test {
        enableAssertions = true
        useJUnitPlatform {}
    }
}
