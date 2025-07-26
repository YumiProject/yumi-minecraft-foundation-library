rootProject.name = "yumi-mc-foundation"

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
