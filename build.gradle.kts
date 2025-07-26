import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import yumimc.Constants
import yumimc.task.FabricTransformJar

plugins {
	id("yumi-mc-core")
	alias(libs.plugins.licenser)
	alias(libs.plugins.nexus.publish)
	`maven-publish`
	signing
}

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
			this.withEntrypoints("yumi:init", "dev.yumi.mc.core.impl.YumiModsImpl::INSTANCE")
			this.withDepend("minecraft", "~1.21")
			this.withDepend("java", ">=${Constants.JAVA_VERSION}")
			this.withMixins("yumi_mc_core.mixins.json")
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
			this.withDepend("minecraft", "[" + libs.versions.minecraft.get() + ",)")
			this.withMixins("yumi_mc_core.mixins.json")
			this.withCustom("\"yumi:entrypoints\".\"yumi:init\"", "dev.yumi.mc.core.impl.YumiModsImpl::INSTANCE")
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
		}
	}
}

val testmod: SourceSet by sourceSets.creating {
	this.compileClasspath += sourceSets.main.get().compileClasspath
	this.runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

dependencies {
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		mappings("dev.lambdaurora:yalmm:${Constants.mcVersion()}+build.${libs.versions.mappings.yalmm.get()}")
	})

	api(libs.yumi.commons.core)
	api(libs.yumi.commons.collections)
	api(libs.yumi.commons.event)
	include(libs.yumi.commons.core)
	include(libs.yumi.commons.collections)
	include(libs.yumi.commons.event)

	modImplementation(libs.fabric.loader)
	compileOnly(libs.neoforge.loader)

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
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

tasks.javadoc {
	source = source.filter {
		!it.startsWith(project.file("src/main/java/dev/yumi/mc/core/mixin"))
				&& !it.startsWith(project.file("src/main/java/dev/yumi/mc/core/impl"))
	}.asFileTree
	options {
		this as StandardJavadocDocletOptions

		addStringOption("Xdoclint:all,-missing", "-quiet")
	}
}

//region Testmod
val testmodJar = tasks.register<Jar>("testmodJar") {
	this.group = "build"
	this.from(testmod.output)
	this.archiveClassifier = "testmod-dev"
	this.destinationDirectory = project.file("build/devlibs")
}

val remapTestmodJar = tasks.register<RemapJarTask>("remapTestmodJar") {
	this.group = "build"
	this.dependsOn(testmodJar.get())
	this.inputFile.set(testmodJar.get().archiveFile)
	this.classpath.from(testmod.compileClasspath)
	this.archiveClassifier = "testmod"
}
tasks.build.get().dependsOn(remapTestmodJar)
//endregion

license {
	rule(rootProject.file("codeformat/HEADER"))
}

//region Mojmap
val mojmap = lambdamcdev.setupMojmapRemapping()

tasks.remapJar.configure {
	this.archiveClassifier = "unprocessed"
	this.destinationDirectory = layout.buildDirectory.map { directory -> directory.dir("devlibs") }
}

val processFabric by tasks.registering(FabricTransformJar::class) {
	this.group = "build"
	this.dependsOn(tasks.remapJar)
	inputJar.set(tasks.remapJar.flatMap { it.archiveFile })
}

afterEvaluate {
	lambdamcdev.replaceArtifactInConfiguration(
		JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME, processFabric
	)
	lambdamcdev.replaceArtifactInConfiguration(
		JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME, processFabric
	)
}

val remapMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "build"
	this.dependsOn(tasks.remapJar)

	inputFile.set(tasks.remapJar.flatMap { it.archiveFile })
	customMappings.from(mojmap.mappingsConfiguration())
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	addNestedDependencies = false // Jars have already been included in the remapJar task

	mojmap.setJarArtifact(this)
}

val remapTestmodMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "build"
	this.dependsOn(remapTestmodJar)

	inputFile.set(remapTestmodJar.flatMap { it.archiveFile })
	customMappings.from(mojmap.mappingsConfiguration())
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "testmod-mojmap"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	addNestedDependencies = false // Jars have already been included in the remapJar task
}

tasks.assemble.configure { this.dependsOn(processFabric, remapMojmap, remapTestmodMojmap) }

val remapMojmapSources by tasks.registering(RemapSourcesJarTask::class) {
	dependsOn(tasks.remapSourcesJar)

	inputFile.set(tasks.remapSourcesJar.flatMap { it.archiveFile })
	customMappings.from(mojmap.mappingsConfiguration())
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap-sources"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))
}

// Add the remapped sources artifact
mojmap.setSourcesArtifact(remapMojmapSources)
//endregion

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
