import dev.lambdaurora.mcdev.api.McVersionLookup
import yumimc.Constants

plugins {
	id("yumi-mc-core")
	alias(libs.plugins.licenser)
	alias(libs.plugins.nexus.publish)
	`maven-publish`
	signing
}

version = "${version}+${McVersionLookup.getVersionTag(Constants.mcVersion())}"
base.archivesName.set(project.property("archives_base_name") as String)

lambdamcdev {
	manifests {
		val fmj = fmj {
			this.withName(Constants.PROJECT_NAME)
			this.withDescription(Constants.PROJECT_DESCRIPTION)
			this.withAuthors(Constants.DEVELOPERS.stream().map { it.name }.toList())
			this.withContact {
				it.withHomepage(Constants.PROJECT_URL)
					.withSources(Constants.GIT_URL)
					.withIssues(Constants.ISSUES_URL)
			}
			this.withLicense(Constants.LICENSE_NAME)
			this.withEntrypoints("yumi:init", "dev.yumi.mc.core.impl.YumiFoundationMod")
			this.withDepend("minecraft", "~26.1-")
			this.withDepend("java", ">=${Constants.JAVA_VERSION}")
			this.withDepend("yumi_commons_event", "~${libs.versions.yumi.commons.get()}")
			this.withMixins("yumi_mc_core.mixins.json", "yumi_mc_core.neoforge.mixins.json")
			this.withModMenu {
				it.withBadges("library")
				it.withParent("yumi_mc_libraries", "Yumi Minecraft Libraries") { parent ->
					parent.withBadges("library")
				}
			}
		}
		nmt {
			fmj.copyTo(this)
			this.withLoaderVersion("[2,)")
			this.withDepend("minecraft", "[${libs.versions.minecraft.get()},)")
			this.withMixins("yumi_mc_core.mixins.json", "yumi_mc_core.neoforge.mixins.json")
			this.withCustom("\"yumi:entrypoints\".\"yumi:init\"", "dev.yumi.mc.core.impl.YumiFoundationMod")
		}
	}

	setupJarJarCompat()
}

repositories {
	maven {
		name = "Gegy"
		url = uri("https://maven.gegy.dev/releases/")
	}
	maven {
		name = "NeoForge"
		url = uri("https://maven.neoforged.net/")
		content {
			includeGroupByRegex("net\\.neoforged.*")
			includeGroupByRegex("cpw\\.mods.*")
			includeGroupAndSubgroups("net.minecraftforge")
		}
	}
}

val testmod: SourceSet by sourceSets.creating {
	this.compileClasspath += sourceSets.main.get().compileClasspath
	this.runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

afterEvaluate {
	val shims: SourceSet by sourceSets.creating {
		this.compileClasspath += configurations["minecraftNamedCompile"]
		sourceSets.main.get().compileClasspath += this.output
	}
}

dependencies {
	api(libs.jspecify)
	api(libs.yumi.commons.core) {
		// Exclude Minecraft and loader-provided libraries.
		exclude(group = "org.slf4j")
		exclude(group = "org.ow2.asm")
	}
	api(libs.yumi.commons.collections) {
		// Exclude Minecraft and loader-provided libraries.
		exclude(group = "org.slf4j")
		exclude(group = "org.ow2.asm")
	}
	api(libs.yumi.commons.event) {
		// Exclude Minecraft and loader-provided libraries.
		exclude(group = "org.slf4j")
		exclude(group = "org.ow2.asm")
	}
	include(libs.yumi.commons.core)
	include(libs.yumi.commons.collections)
	include(libs.yumi.commons.event)

	compileOnly(libs.fabric.loader)
	compileOnly(libs.neoforge.loader)
	localRuntime(libs.fabric.loader)

	"testmodImplementation"(sourceSets.main.get().output)
}

loom {
	@Suppress("UnstableApiUsage")
	mixin {
		useLegacyMixinAp = false
	}
	runs {
		register("testmodClient") {
			configName = "Testmod Client"
			client()
			source(testmod)
		}
		register("testServer") {
			configName = "Testmod Server"
			server()
			source(testmod)
		}
	}
}

tasks.jar {
	dependsOn(tasks.processIncludeJars)
	inputs.property("archivesName", base.archivesName)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

tasks.javadoc {
	val exclude = listOf(
		project.file("src/main/java/dev/yumi/mc/core/impl"),
		project.file("src/main/java/dev/yumi/mc/core/mixin")
	)

	source = source.filter { sourceIt -> !exclude.any { sourceIt.startsWith(it) } }.asFileTree
	options {
		this as StandardJavadocDocletOptions

		addStringOption("Xdoclint:all,-missing", "-quiet")
		links(
			"https://jspecify.dev/docs/api/",
			"https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/",
			"https://javadoc.io/doc/dev.yumi.commons/yumi-commons-core/${libs.versions.yumi.commons.get()}/",
			"https://javadoc.io/doc/dev.yumi.commons/yumi-commons-collections/${libs.versions.yumi.commons.get()}/",
			"https://javadoc.io/doc/dev.yumi.commons/yumi-commons-event/${libs.versions.yumi.commons.get()}/"
		)
	}
}

//region Testmod
val testmodJar = tasks.register<Jar>("testmodJar") {
	this.group = "build"
	this.from(testmod.output)
	this.archiveClassifier = "testmod"
}

tasks.build.get().dependsOn(testmodJar)
//endregion

license {
	rule(rootProject.file("codeformat/HEADER"))
	excludeSourceSet("shims")
}

// Setup publishing of artifacts.
publishing {
	publications {
		create<MavenPublication>(Constants.PUBLICATION_NAME) {
			from(components["java"])
			artifactId = "yumi-mc-foundation"

			pom {
				name = Constants.PROJECT_NAME
				description = Constants.PROJECT_DESCRIPTION
				url = Constants.PROJECT_URL

				organization {
					name = Constants.ORG_NAME
					url = Constants.ORG_URL
				}

				developers {
					Constants.DEVELOPERS.forEach {
						developer {
							name = it.name
							email = it.email
						}
					}
				}

				licenses {
					license {
						name = Constants.LICENSE_NAME
						url = Constants.LICENSE_URL
					}
				}

				scm {
					url = Constants.GIT_URL
					connection = Constants.GIT_CONNECTION
					developerConnection = Constants.GIT_DEV_CONNECTION
				}
			}
		}
	}

	repositories {
		mavenLocal()
		maven {
			name = "BuildDirLocal"
			url = uri("${layout.buildDirectory.get()}/repo")
		}
	}
}

nexusPublishing {
	repositories {
		val mavenCentralKey: String? by project
		val mavenCentralSecret: String? by project

		if (mavenCentralKey != null && mavenCentralSecret != null) {
			sonatype {
				username = mavenCentralKey
				password = mavenCentralSecret

				nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
				snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
			}
		}
	}
}

// Setup signing.
signing {
	val signingKeyId: String? by rootProject
	val signingKey: String? by rootProject
	val signingPassword: String? by rootProject
	isRequired = signingKeyId != null && signingKey != null && signingPassword != null
	useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)

	sign(publishing.publications[Constants.PUBLICATION_NAME])

	afterEvaluate {
		tasks["sign${Constants.PUBLICATION_NAME.replaceFirstChar(Char::titlecase)}Publication"].group = "publishing"
	}
}
