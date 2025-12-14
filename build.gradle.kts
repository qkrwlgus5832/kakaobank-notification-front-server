val loggerVersion by extra { "2.0.13" }
val httpClientVersion by extra { "4.5.14" }
val mockKVersion by extra { "1.13.10" }
val springMockkVersion by extra { "4.0.2" }
val springDocUiVersion by extra { "2.5.0" }

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("kapt") version "1.9.23"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.5" apply false
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    if (project.childProjects.isNotEmpty()) {
        return@subprojects
    }

    group = "com.example"
    version = "0.0.1-SNAPSHOT"
    description = "notifiaction-front-server"

    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("kotlin")
        plugin("kotlin-spring")
        plugin("kotlin-kapt")
        plugin("org.jetbrains.kotlin.plugin.jpa")
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("io.mockk:mockk:$mockKVersion")
    }

    noArg {
        annotation("jakarta.persistence.Entity")
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

