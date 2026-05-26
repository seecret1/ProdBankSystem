import org.gradle.api.publish.maven.MavenPublication

val versions = mapOf(
	"jakartaValidationVersion" to "3.0.2",
	"fasterxmlJacksonVersion" to "2.17.2",
	"lombokVersion" to "1.18.34",
	"springdocOpenapi" to "2.5.0"
)

plugins {
	idea
	java
	id("maven-publish")
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.seecret1"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.4")
	}
}

configurations.all { resolutionStrategy.cacheChangingModulesFor(0, "seconds") }

dependencies {
	implementation("jakarta.validation:jakarta.validation-api:${versions["jakartaValidationVersion"]}")
	implementation("com.fasterxml.jackson.core:jackson-annotations:${versions["fasterxmlJacksonVersion"]}")

	compileOnly("org.projectlombok:lombok:${versions["lombokVersion"]}")
	compileOnly("org.springdoc:springdoc-openapi-starter-common:${versions["springdocOpenapi"]}")

	annotationProcessor("org.projectlombok:lombok:${versions["lombokVersion"]}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jar {
	enabled = true
	archiveBaseName.set(project.name)
	archiveVersion.set(project.version.toString())

	manifest {
		attributes(
			"Implementation-Title" to project.name,
			"Implementation-Version" to project.version
		)
	}
}

/*
──────────────────────────────────────────────────────
============== Resolve NEXUS credentials ==============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach {
	if (it.contains("=") && !it.startsWith("#")) {
		val (k, v) = it.split("=", limit = 2)
		System.setProperty(k.trim(), v.trim())
		logger.lifecycle("Loaded env: ${k.trim()}=${v.trim()}")
	}
}

val nexusUrl = System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL")
val nexusUser = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME")
val nexusPassword = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD")

if (nexusUrl.isNullOrBlank() || nexusUser.isNullOrBlank() || nexusPassword.isNullOrBlank()) {
	throw GradleException(
		"""
        NEXUS credentials are missing!
        
        Create .env file in project root with:
        NEXUS_URL=http://your-nexus:8081/repository/maven-releases/
        NEXUS_USERNAME=your-username
        NEXUS_PASSWORD=your-password
        """.trimIndent()
	)
}

/*
──────────────────────────────────────────────────────
============== Nexus Publishing ==============
──────────────────────────────────────────────────────
*/

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = project.group.toString()
			artifactId = project.name
			version = project.version.toString()

			from(components["java"])

			pom {
				name.set(project.name)
				description.set("Common library for Bank Card Management System")
				url.set("https://github.com/seecret1/ProdBankSystem")

				licenses {
					license {
						name.set("Apache License 2.0")
						url.set("https://www.apache.org/licenses/LICENSE-2.0")
					}
				}

				developers {
					developer {
						id.set("seecret1")
						name.set("seecret1")
						email.set("support@bankapp.com")
					}
				}

				scm {
					connection.set("scm:git:https://github.com/seecret1/ProdBankSystem.git")
					developerConnection.set("scm:git:ssh://github.com/seecret1/ProdBankSystem.git")
					url.set("https://github.com/seecret1/ProdBankSystem")
				}
			}
		}
	}

	repositories {
		maven {
			name = "nexus"
			url = uri(nexusUrl)
			isAllowInsecureProtocol = true
			credentials {
				username = nexusUser
				password = nexusPassword
			}
		}
	}
}

// Дополнительная задача для проверки публикации
tasks.register("checkPublication") {
	doLast {
		val separator = "=".repeat(60)
		println(separator)
		println("📦 Publication details:")
		println("   GroupId: ${project.group}")
		println("   ArtifactId: ${project.name}")
		println("   Version: ${project.version}")
		println("   Repository: $nexusUrl")
		println(separator)
	}
}

tasks.named("publish") {
	dependsOn("checkPublication")
	doFirst {
		println("🚀 Publishing to Nexus...")
	}
	doLast {
		println("✅ Publishing completed!")
		val nexusPath = nexusUrl + project.group.toString().replace('.', '/') + "/" + project.name + "/" + project.version + "/"
		println("📌 Check at: $nexusPath")
	}
}