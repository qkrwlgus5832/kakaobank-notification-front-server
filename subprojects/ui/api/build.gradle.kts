val springMockkVersion: String by rootProject.extra
val springDocUiVersion: String by rootProject.extra

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api(project(":application:service"))
    implementation(project(":application:scheduler"))
    implementation(project(":application:notification-kafka-consumer"))
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocUiVersion")
}
