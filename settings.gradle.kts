rootProject.name = "Yumi Minecraft Libraries - Foundation"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "FabricMC"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Gegy"
			url = uri("https://maven.gegy.dev/releases/")
		}
	}
}

includeBuild("build_logic")
