dependencies {
    api(project(":infra"))
    implementation(project(":application:service"))
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}