import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
}

group = "com.soulf"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val springCloudVersion by extra("2023.0.0")
val lombokVersion by extra("1.18.42")

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Spring Data Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2:2.4.240")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "22"
    targetCompatibility = "22"
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("catalog-service-${version}.jar")
}

