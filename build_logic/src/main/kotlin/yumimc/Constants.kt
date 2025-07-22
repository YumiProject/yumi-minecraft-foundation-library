package yumimc

import org.gradle.accessors.dm.LibrariesForLibs

object Constants {
	const val GROUP = "dev.yumi.mc.core"
	const val NAMESPACE = "yumi_mc_core"

	const val VERSION = "1.0.0-alpha.1"
	const val JAVA_VERSION = 21

	const val PROJECT_NAME = "Yumi Minecraft Libraries: Foundation"
	const val PROJECT_DESCRIPTION = "A core library offering entrypoints and environment-related utilities."

	private var minecraftVersion: String? = null

	fun finalizeInit(libs: LibrariesForLibs) {
		this.minecraftVersion = libs.versions.minecraft.get()
	}

	fun mcVersion(): String {
		return this.minecraftVersion!!
	}
}