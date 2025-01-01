import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.serialization") version "2.1.0"
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
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

	implementation("org.web3j:core:4.9.0")
	implementation("org.web3j:crypto:4.9.0")

	implementation("com.nimbusds:nimbus-jose-jwt:9.48")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.1.0")
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
