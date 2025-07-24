import org.gradle.accessors.dm.LibrariesForLibs
import yumimc.Constants

plugins {
	id("fabric-loom")
	id("dev.lambdaurora.mcdev")
}

// Seriously you should not worry about it, definitely not a hack.
// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()
Constants.finalizeInit(libs)

group = Constants.GROUP
version = "${Constants.VERSION}+${Constants.mcVersion()}"
lambdamcdev.namespace = Constants.NAMESPACE

repositories {
	mavenCentral()
}

dependencies {
	minecraft(libs.minecraft)
}

java {
	sourceCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)
	targetCompatibility = JavaVersion.toVersion(Constants.JAVA_VERSION)

	withSourcesJar()
	withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(Constants.JAVA_VERSION)
}
