/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.impl.entrypoint.EntrypointStorage;
import dev.yumi.mc.core.impl.entrypoint.JoinedEntrypointStorage;
import dev.yumi.mc.core.impl.mod.ExtendedModContainer;
import dev.yumi.mc.core.impl.mod.FabricModContainer;
import dev.yumi.mc.core.impl.mod.NeoModContainer;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class YumiModsImpl implements YumiMods {
	public static final YumiModsImpl INSTANCE = new YumiModsImpl();
	final List<CurrentRuntime> runtimes;
	private final CurrentRuntime runtime;
	private final Map<String, ExtendedModContainer> modsMap = new HashMap<>();
	private final List<ExtendedModContainer> mods = new ArrayList<>();
	private final EntrypointStorage entrypointStorage;

	public YumiModsImpl() {
		var runtimes = new ArrayList<CurrentRuntime>();
		if (EnvironmentUtils.FABRIC) {
			runtimes.add(new CurrentRuntime.FabricRuntime());
		}
		if (EnvironmentUtils.NEOFORGE) {
			runtimes.add(new CurrentRuntime.NeoForgeRuntime());
		}
		this.runtimes = List.copyOf(runtimes);

		this.runtime = this.runtimes.getFirst();

		var initializers = List.<Supplier<Consumer<List<ExtendedModContainer>>>>of(
				() -> NeoModContainer::init,
				() -> FabricModContainer::init
		);

		var mods = new ArrayList<ExtendedModContainer>();

		var errors = new ArrayList<Error>();
		for (var initializer : initializers) {
			try {
				initializer.get().accept(mods);
			} catch (LinkageError e) {
				errors.add(e);
			}
		}

		if (errors.size() == initializers.size()) {
			var error = errors.stream().reduce((a, b) -> {
				a.addSuppressed(b);
				return a;
			}).orElseThrow();
			throw new IllegalStateException("Failed to initialize ModManager: failed to find any mod loader.", error);
		}

		var directModsMap = new HashMap<String, ExtendedModContainer>();
		for (var mod : mods) {
			directModsMap.put(mod.id(), mod);
		}

		this.mods.addAll(directModsMap.values());
		this.modsMap.putAll(directModsMap);
		for (var mod : directModsMap.values()) {
			for (var provided : mod.getProvidedIds()) {
				this.modsMap.put(provided, mod);
			}
		}

		this.entrypointStorage = JoinedEntrypointStorage.init(mods);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return this.runtime.isDevelopmentEnvironment();
	}

	@Override
	public Path getGameDirectory() {
		return this.runtime.getGameDirectory();
	}

	@Override
	public Path getConfigDirectory() {
		return this.runtime.getConfigDirectory();
	}

	@Override
	public Optional<ModContainer> getMod(String id) {
		return Optional.ofNullable(this.modsMap.get(id));
	}

	@Override
	public boolean isModLoaded(String id) {
		return this.modsMap.containsKey(id);
	}

	@Override
	public Collection<ModContainer> getMods() {
		return Collections.unmodifiableList(this.mods);
	}

	@Override
	public <T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type) {
		return this.entrypointStorage.getEntrypoints(key, type);
	}
}
