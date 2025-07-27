/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api;

import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.impl.YumiModsImpl;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface YumiMods {
	static YumiMods get() {
		return YumiModsImpl.INSTANCE;
	}

	/**
	 * {@return {@code true} if the current environment is a development environment, or {@code false} otherwise}
	 */
	boolean isDevelopmentEnvironment();

	/**
	 * {@return the current game working directory}
	 */
	Path getGameDirectory();

	/**
	 * {@return the current directory for game configuration files}
	 */
	Path getConfigDirectory();

	/**
	 * Gets the container for a given mod.
	 *
	 * @param id the identifier of the mod
	 * @return the mod container, if present
	 */
	Optional<ModContainer> getMod(String id);

	/**
	 * {@return {@code true} if the given mod is loaded, or {@code false} otherwise}
	 *
	 * @param id the identifier of the mod
	 */
	boolean isModLoaded(String id);

	/**
	 * Gets all mod containers.
	 *
	 * @return a collection of all loaded mod containers
	 */
	@UnmodifiableView
	Collection<ModContainer> getMods();

	/**
	 * Gets all entrypoints for a given {@code key} and {@code type}.
	 *
	 * @param key the key of the entrypoint
	 * @param type the corresponding type of the entrypoint
	 * @return a list of the found entrypoints
	 * @param <T> the type of the entrypoint
	 * @see #invokeEntrypoints(String, Class, BiConsumer)
	 */
	<T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type);

	/**
	 * Invokes the entrypoints of a given {@code key} and {@code type} to do the given {@code action}.
	 *
	 * @param key the key of the entrypoint
	 * @param type the corresponding type of the entrypoint
	 * @param action the action to do to invoke the found entrypoints
	 * @param <T> the type of the entrypoint
	 * @see #getEntrypoints(String, Class)
	 */
	default <T> void invokeEntrypoints(String key, Class<T> type, BiConsumer<T, ModContainer> action) {
		var entrypoints = this.getEntrypoints(key, type);
		for (var entrypoint : entrypoints) {
			action.accept(entrypoint.value(), entrypoint.mod());
		}
	}
}
