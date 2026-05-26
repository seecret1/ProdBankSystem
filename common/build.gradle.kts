import org.gradle.api.GradleException
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import java.io.File

val versions = mapOf(
	"springBootBoomVersion" to "3.5.8",
	"jakartaValidationVersion" to "3.0.2",
	"fasterxmlJacksonVersion" to "2.17.2",
	"lombokVersion" to "1.18.34",
	"springdocOpenapi" to "2.5.0"
)

plugins {
	idea
	`java-library`
	`maven-publish`
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.github.seecret1"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.boot:spring-boot-dependencies:${versions["springBootBoomVersions"]}")
	}
}

dependencies {
	implementation("jakarta.validation:jakarta.validation-api:${versions["jakartaValidationVersion"]}")
	implementation("com.fasterxml.jackson.core:jackson-annotations:${versions["fasterxmlJacksonVersion"]}")

	compileOnly("org.projectlombok:lombok:${versions["lombokVersion"]}")
	compileOnly("org.springdoc:springdoc-openapi-starter-common:${versions["springdocOpenapi"]}")
}

tasks.test {
	useJUnitPlatform()
}

/*
──────────────────────────────────────────────────────
============== Specifications ==============
──────────────────────────────────────────────────────
*/

val specsDir = file("specifications")

val foundSpecifications: List<File> =
	specsDir
		.listFiles()
		?.filter {
			it.extension == "yaml" ||
					it.extension == "yml" ||
					it.extension == "json"
		}
		?: emptyList()

fun buildGenerateApiTaskName(specName: String): String =
	"generate${specName.replaceFirstChar(Char::uppercase)}Api"

fun buildJarTaskName(specName: String): String =
	"${specName}Jar"

fun generatedOutputDir(specName: String) =
	layout.buildDirectory.dir("generated/$specName")

val sourceSets = the<SourceSetContainer>()

/*
──────────────────────────────────────────────────────
============== Generated JARs ==============
──────────────────────────────────────────────────────
*/

val generatedJarTasks = mutableListOf<TaskProvider<Jar>>()

foundSpecifications.forEach { specFile ->

	val specName = specFile.nameWithoutExtension

	val sourceSetName = specName

	val generatedJavaDir =
		generatedOutputDir(specName).map {
			it.asFile.resolve("src/main/java")
		}

	val sourceSet = sourceSets.create(sourceSetName) {

		java.srcDir(generatedJavaDir)

		compileClasspath += sourceSets["main"].compileClasspath
		runtimeClasspath += output + compileClasspath
	}

	/*
    ──────────────────────────────────────────────────────
    ============== Generate Sources ==============
    ──────────────────────────────────────────────────────
    */

	val generateTask = tasks.register(
		buildGenerateApiTaskName(specName)
	) {

		outputs.dir(generatedOutputDir(specName))

		doLast {

			val outputDir =
				generatedOutputDir(specName)
					.get()
					.asFile
					.resolve("src/main/java/com/generated/$specName")

			outputDir.mkdirs()

			val className =
				specName.replaceFirstChar(Char::uppercase)

			val generatedFile =
				outputDir.resolve("${className}Api.java")

			generatedFile.writeText(
				"""
                package com.generated.$specName;

                public class ${className}Api {

                    public static String name() {
                        return "$specName";
                    }
                }
                """.trimIndent()
			)

			println("Generated sources for: $specName")
		}
	}

	/*
    ──────────────────────────────────────────────────────
    ============== Compile ==============
    ──────────────────────────────────────────────────────
    */

	val compileTask = tasks.register<JavaCompile>(
		"compile${sourceSetName.replaceFirstChar(Char::uppercase)}Java"
	) {

		dependsOn(generateTask)

		source = sourceSet.java

		classpath = sourceSet.compileClasspath

		destinationDirectory.set(
			layout.buildDirectory.dir("classes/$sourceSetName")
		)

		options.encoding = "UTF-8"
	}

	/*
    ──────────────────────────────────────────────────────
    ============== JAR ==============
    ──────────────────────────────────────────────────────
    */

	val jarTask = tasks.register<Jar>(
		buildJarTaskName(specName)
	) {

		group = "build"

		dependsOn(compileTask)

		archiveBaseName.set(specName)

		archiveVersion.set(project.version.toString())

		destinationDirectory.set(
			layout.buildDirectory.dir("libs")
		)

		from(
			layout.buildDirectory.dir("classes/$sourceSetName")
		)

		doFirst {
			println("Building JAR for: $specName")
		}
	}

	generatedJarTasks += jarTask
}

/*
──────────────────────────────────────────────────────
============== Build Lifecycle ==============
──────────────────────────────────────────────────────
*/

tasks.named("build") {
	dependsOn(generatedJarTasks)
}

/*
──────────────────────────────────────────────────────
============== Load .env ==============
──────────────────────────────────────────────────────
*/

file(".env")
	.takeIf { it.exists() }
	?.readLines()
	?.forEach { line ->

		if (line.contains("=")) {

			val (key, value) =
				line.split("=", limit = 2)

			System.setProperty(
				key.trim(),
				value.trim()
			)
		}
	}

/*
──────────────────────────────────────────────────────
============== Nexus Credentials ==============
──────────────────────────────────────────────────────
*/

val nexusUrl =
	System.getenv("NEXUS_URL")
		?: System.getProperty("NEXUS_URL")

val nexusUser =
	System.getenv("NEXUS_USERNAME")
		?: System.getProperty("NEXUS_USERNAME")

val nexusPassword =
	System.getenv("NEXUS_PASSWORD")
		?: System.getProperty("NEXUS_PASSWORD")

if (
	nexusUrl.isNullOrBlank() ||
	nexusUser.isNullOrBlank() ||
	nexusPassword.isNullOrBlank()
) {
	throw GradleException(
		"""
        Nexus credentials are missing.

        Required:
        - NEXUS_URL
        - NEXUS_USERNAME
        - NEXUS_PASSWORD
        """.trimIndent()
	)
}

/*
──────────────────────────────────────────────────────
============== Publishing ==============
──────────────────────────────────────────────────────
*/

publishing {

	publications {

		foundSpecifications.forEach { specFile ->

			val specName =
				specFile.nameWithoutExtension

			create<MavenPublication>(
				"publish${specName.replaceFirstChar(Char::uppercase)}"
			) {

				groupId = project.group.toString()

				artifactId = specName

				version = project.version.toString()

				artifact(
					tasks.named(
						buildJarTaskName(specName)
					)
				)

				pom {

					name.set("Generated API: $specName")

					description.set(
						"OpenAPI generated library for $specName"
					)
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