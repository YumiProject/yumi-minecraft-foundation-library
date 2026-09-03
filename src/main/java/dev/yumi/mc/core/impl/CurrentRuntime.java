/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.mc.core.impl.neoforge.DeferredRegisterUndeferrer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;

interface CurrentRuntime {
	boolean isDevelopmentEnvironment();

	Path getGameDirectory();

	Path getConfigDirectory();

	default void init() {}

	class FabricRuntime implements CurrentRuntime {
		@Override
		public boolean isDevelopmentEnvironment() {
			return FabricLoader.getInstance().isDevelopmentEnvironment();
		}

		@Override
		public Path getGameDirectory() {
			return FabricLoader.getInstance().getGameDir();
		}

		@Override
		public Path getConfigDirectory() {
			return FabricLoader.getInstance().getConfigDir();
		}
	}

	class NeoForgeRuntime implements CurrentRuntime {
		@Override
		public boolean isDevelopmentEnvironment() {
			return !FMLEnvironment.isProduction();
		}

		@Override
		public Path getGameDirectory() {
			return FMLPaths.GAMEDIR.get();
		}

		@Override
		public Path getConfigDirectory() {
			return FMLPaths.CONFIGDIR.get();
		}

		@Override
		public void init() {
			for (var field : NeoForgeMod.class.getDeclaredFields()) {
				if (DeferredRegisterUndeferrer.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					try {
						var register = (DeferredRegister<?>) field.get(null);
						if (register.getRegistryName().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
							((DeferredRegisterUndeferrer) register).yumi$registerNow();
						}
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
