val loggerVersion: String by rootProject.extra

dependencies {
    api("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.slf4j:slf4j-api:$loggerVersion")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}