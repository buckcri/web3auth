import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.2"
	id("io.spring.dependency-management") version "1.0.12.RELEASE"
	kotlin("jvm") version "1.7.10"
	kotlin("plugin.spring") version "1.7.10"
	kotlin("plugin.serialization") version "1.7.10"
}

group = "com.github.buckcri.web3auth"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

val springdocOpenapiVersion by extra { "1.6.9" }

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

	implementation("org.web3j:core:4.9.0")
	implementation("org.web3j:crypto:4.9.0")

	implementation("com.nimbusds:nimbus-jose-jwt:9.23")

	implementation("org.springdoc:springdoc-openapi-ui:$springdocOpenapiVersion")
	implementation("org.springdoc:springdoc-openapi-kotlin:$springdocOpenapiVersion")
	implementation("org.springdoc:springdoc-openapi-webmvc-core:$springdocOpenapiVersion")

	implementation("org.zalando:problem-spring-web-starter:0.28.0-RC.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
