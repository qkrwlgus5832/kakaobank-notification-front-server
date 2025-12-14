val httpClientVersion: String by rootProject.extra

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.apache.httpcomponents:httpclient:$httpClientVersion")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}