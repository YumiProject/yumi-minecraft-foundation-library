# Yumi Minecraft Libraries - Foundation

![Java 21](https://img.shields.io/badge/language-Java%2021-9115ff.svg?style=flat-square)
[![GitHub license](https://img.shields.io/github/license/YumiProject/yumi-minecraft-foundation-library?style=flat-square)](https://raw.githubusercontent.com/YumiProject/yumi-minecraft-foundation-library/1.21/LICENSE)
![Maven Central](https://img.shields.io/maven-central/v/dev.yumi.mc.core/yumi-mc-foundation?style=flat-square&label=Maven%20Central)

A library for Minecraft mods providing multiloader foundational features, such as entrypoints, ways to identify other running mods, and events.

## Usage

### Import

With loom:

```kotlin
dependencies {
	modImplementation("dev.yumi.mc.core:yumi-mc-foundation:version")
}
```

In a Mojang mapped environment:
```kotlin
dependencies {
	implementation("dev.yumi.mc.core:yumi-mc-foundation:version") {
		capabilities {
			requireCapability("dev.yumi.mc.core:yumi-mc-foundation-mojmap")
		}
	}
}
```
