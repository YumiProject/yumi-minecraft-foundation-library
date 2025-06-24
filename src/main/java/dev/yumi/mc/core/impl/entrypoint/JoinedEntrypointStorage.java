/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.entrypoint;

import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.impl.EnvironmentUtils;
import dev.yumi.mc.core.impl.mod.ExtendedModContainer;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class JoinedEntrypointStorage implements EntrypointStorage {
	private final List<EntrypointStorage> layers;

	public JoinedEntrypointStorage(List<EntrypointStorage> layers) {
		this.layers = layers;
	}

	@Override
	public <T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type) {
		var list = new ArrayList<EntrypointContainer<T>>();
		for (var layer : layers) {
			list.addAll(layer.getEntrypoints(key, type));
		}
		return list;
	}

	public static EntrypointStorage init(List<ExtendedModContainer> mods) {
		var layers = new ArrayList<EntrypointStorage>();
		layers.add(new CommonEntrypointStorage(mods));

		if (EnvironmentUtils.FABRIC) {
			layers.add(new FabricEntrypointStorage());
		}

		return new JoinedEntrypointStorage(layers);
	}
}
