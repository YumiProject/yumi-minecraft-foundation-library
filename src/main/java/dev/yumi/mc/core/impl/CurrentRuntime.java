/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.mc.core.impl.neoforge.DeferredRegisterUndeferrer;
import dev.yumi.mc.core.mixin.neoforge.NeoForgeModAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

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
			return !FMLEnvironment.production;
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
			((DeferredRegisterUndeferrer) NeoForgeModAccessor.getATTRIBUTES()).yumi$registerNow();
			((DeferredRegisterUndeferrer) NeoForgeModAccessor.getCOMMAND_ARGUMENT_TYPES()).yumi$registerNow();
			((DeferredRegisterUndeferrer) NeoForgeModAccessor.getENTITY_PREDICATE_CODECS()).yumi$registerNow();
			((DeferredRegisterUndeferrer) NeoForgeModAccessor.getITEM_SUB_PREDICATES()).yumi$registerNow();
		}
	}
}
