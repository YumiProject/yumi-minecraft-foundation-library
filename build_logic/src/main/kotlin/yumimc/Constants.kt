package yumimc

import org.gradle.accessors.dm.LibrariesForLibs

data class Developer(val name: String, val email: String)

object Constants {
	const val GROUP = "dev.yumi.mc.core"
	const val NAMESPACE = "yumi_mc_core"

	const val VERSION = "1.0.0-alpha.4"
	const val JAVA_VERSION = 21

	const val PROJECT_NAME = "Yumi Minecraft Libraries: Foundation"
	const val PROJECT_URL = "https://github.com/YumiProject/yumi-minecraft-foundation-library"
	const val PROJECT_DESCRIPTION = "A core library offering entrypoints and environment-related utilities to Minecraft mods."

	const val ORG_NAME = "Yumi Project"
	const val ORG_URL = "https://yumi.dev/"

	val DEVELOPERS = listOf(
		Developer("$ORG_NAME Minecraft Libraries Development Team", "infra@yumi.dev"),
		Developer("LambdAurora", "email@lambdaurora.dev"),
	)

	const val LICENSE_NAME = "Mozilla Public License Version 2.0"
	const val LICENSE_URL = "https://www.mozilla.org/en-US/MPL/2.0/"

	private const val GIT_REPO = "github.com/YumiProject/yumi-minecraft-foundation-library"
	const val GIT_URL = "https://$GIT_REPO"
	const val GIT_CONNECTION = "scm:git:git://$GIT_REPO"
	val GIT_DEV_CONNECTION = "scm:git:ssh://" + GIT_REPO.replaceFirst('/', ':')

	const val ISSUES_URL = "$GIT_URL/issues"

	const val PUBLICATION_NAME = "mavenJava"

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}
}