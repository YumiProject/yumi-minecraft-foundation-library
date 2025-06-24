/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.entrypoint;

import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
final class FabricEntrypointStorage implements EntrypointStorage {
	@Override
	public <T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type) {
		return FabricLoader.getInstance().getEntrypointContainers(key, type)
				.stream()
				.map(container -> {
					var mod = YumiMods.get().getMod(container.getProvider().getMetadata().getId()).orElseThrow();
					return new EntrypointContainer<>(mod, container.getEntrypoint());
				})
				.toList();
	}
}
