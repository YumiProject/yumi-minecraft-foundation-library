import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace
import net.fabricmc.loom.task.RemapJarTask
import yumimc.Constants
import yumimc.task.FabricTransformJar

plugins {
	id("yumi-mc-core")
	alias(libs.plugins.licenser)
}

base.archivesName.set(project.property("archives_base_name") as String)

lambdamcdev {
	manifests {
		fmj {
			this.withName("Yumi Minecraft Libraries: Foundation")
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
			this.withName("Yumi Minecraft Libraries: Foundation")
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
	runtimeOnlyLog4j = true
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
val mojmap by sourceSets.creating {}
val mojangMappings by configurations.creating {}

dependencies {
	mojangMappings(loom.officialMojangMappings())
}

tasks.remapJar.configure {
	this.archiveClassifier = "unprocessed"
	this.destinationDirectory = layout.buildDirectory.map { directory -> directory.dir("devlibs") }
}

val processFabric by tasks.registering(FabricTransformJar::class) {
	this.group = "build"
	this.dependsOn(tasks.remapJar)
	inputJar.set(tasks.remapJar.flatMap { it.archiveFile })
}

val remapMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "build"
	this.dependsOn(tasks.remapJar)

	inputFile.set(tasks.remapJar.flatMap { it.archiveFile })
	customMappings.from(mojangMappings)
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "mojmap"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	addNestedDependencies = false // Jars have already been included in the remapJar task
}

val remapTestmodMojmap by tasks.registering(RemapJarTask::class) {
	this.group = "build"
	this.dependsOn(remapTestmodJar)

	inputFile.set(remapTestmodJar.flatMap { it.archiveFile })
	customMappings.from(mojangMappings)
	sourceNamespace = "intermediary"
	targetNamespace = "named"
	archiveClassifier = "testmod-mojmap"
	classpath.setFrom((loom as LoomGradleExtension).getMinecraftJars(MappingsNamespace.INTERMEDIARY))

	addNestedDependencies = false // Jars have already been included in the remapJar task
}

tasks.build.configure {
	this.dependsOn(processFabric, remapMojmap)
}
//endregion
