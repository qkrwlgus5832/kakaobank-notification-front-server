dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(project(":domain"))
    implementation(project(":application:service"))
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}