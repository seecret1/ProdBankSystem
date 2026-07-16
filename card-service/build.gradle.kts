val versions = mapOf(
	"mapstructVersion" to "1.5.5.Final",
	"commonVersion" to "1.0.0-SNAPSHOT",
	"springdocOpenapiStarterWebmvcUiVersion" to "2.5.0",
	"logbackClassicVersion" to "1.5.18",
	"hibernateVersion" to "7.2.0.Final",
	"feignMicrometerVersion" to "13.6"
)

plugins {
	java
	id("org.springframework.boot") version "3.4.8"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.seecret1"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
	mavenLocal()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
		mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.15.0")
	}
}

dependencies {
	implementation("com.github.seecret1:common:${versions["commonVersion"]}@jar")
	implementation("com.github.seecret1:jwt-common:${versions["commonVersion"]}")

	implementation("io.micrometer:micrometer-registry-prometheus")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	implementation("org.springframework.kafka:spring-kafka")

	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springdocOpenapiStarterWebmvcUiVersion"]}")

	implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometerVersion"]}")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

	implementation("ch.qos.logback:logback-classic:${versions["logbackClassicVersion"]}")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")

	implementation("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.hibernate.orm:hibernate-core:${versions["hibernateVersion"]}")
	implementation("org.hibernate.orm:hibernate-envers:${versions["hibernateVersion"]}")

	implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}


/*
──────────────────────────────────────────────────────
============== Resolve NEXUS credentials =============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach {
	val (k, v) = it.split("=", limit = 2)
	System.setProperty(k.trim(), v.trim())
	logger.lifecycle("${k.trim()}=${v.trim()}")
}

val nexusUrl = System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL")
val nexusUser = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME")
val nexusPassword = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD")

if (nexusUrl.isNullOrBlank() || nexusUser.isNullOrBlank() || nexusPassword.isNullOrBlank()) {
	throw GradleException(
		"NEXUS_URL or NEXUS_USER or NEXUS_PASSWORD not set. " +
				"Please create a .env file with these properties or set environment variables."
	)
}

repositories {
	mavenCentral()
	maven {
		url = uri(nexusUrl)
		isAllowInsecureProtocol = true
		credentials {
			username = nexusUser
			password = nexusPassword
		}
	}
}
