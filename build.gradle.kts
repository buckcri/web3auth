import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.20"
	kotlin("plugin.spring") version "2.2.0"
	kotlin("plugin.serialization") version "2.1.20"
}

group = "com.github.buckcri.web3auth"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.github.buckcri.xclacks:xclacks:1.0.0")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

	implementation("org.web3j:core:4.12.3")
	implementation("org.web3j:crypto:4.12.3")

	implementation("com.nimbusds:nimbus-jose-jwt:10.0.2")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.21")
}

tasks.withType<KotlinJvmCompile>().configureEach {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
