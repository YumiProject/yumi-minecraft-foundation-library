/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.commons.event.EventManager;
import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import dev.yumi.mc.core.impl.entrypoint.EntrypointStorage;
import dev.yumi.mc.core.impl.entrypoint.JoinedEntrypointStorage;
import dev.yumi.mc.core.impl.mod.ExtendedModContainer;
import dev.yumi.mc.core.impl.mod.FabricModContainer;
import dev.yumi.mc.core.impl.mod.NeoModContainer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class YumiModsImpl implements YumiMods, ModInitializer {
	public static final YumiModsImpl INSTANCE = new YumiModsImpl();
	public final EventManager<Identifier> eventManager = new EventManager<>(
			Identifier.of("yumi", "default"),
			Identifier::parse
	);
	private final CurrentRuntime runtime;
	private final Map<String, ExtendedModContainer> modsMap = new HashMap<>();
	private final List<ExtendedModContainer> mods = new ArrayList<>();
	private final EntrypointStorage entrypointStorage;

	public YumiModsImpl() {
		this.runtime = EnvironmentUtils.FABRIC ? new CurrentRuntime.FabricRuntime() : new CurrentRuntime.NeoForgeRuntime();

		var initializers = List.<Supplier<Consumer<List<ExtendedModContainer>>>>of(
				() -> NeoModContainer::init,
				() -> FabricModContainer::init
		);

		var errors = new ArrayList<Error>();
		for (var initializer : initializers) {
			try {
				initializer.get().accept(this.mods);
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

		for (var mod : this.mods) {
			this.modsMap.put(mod.id(), mod);

			for (var provided : mod.getProvidedIds()) {
				this.modsMap.put(provided, mod);
			}
		}

		this.entrypointStorage = JoinedEntrypointStorage.init(mods);

		this.eventManager.getCreationEvent().register((manager, event) -> {
			for (var target : EventSideTarget.VALUES) {
				// Search if the callback qualifies is unique to this event.
				if (target.listenerClass().isAssignableFrom(event.type())) {
					List<?> entrypoints = this.getEntrypoints(target.entrypointKey(), target.listenerClass());

					// Search for matching entrypoint.
					for (Object entrypoint : entrypoints) {
						// Searching if the given entrypoint is a listener of the event being registered.
						if (event.type().isAssignableFrom(entrypoint.getClass())) {
							// It is, then register the listener.
							manager.listenAll(entrypoint, event);
						}
					}

					break;
				}
			}
		});
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

	private static final Identifier SYSTEM_DETAILS_POPULATION_PHASE = Identifier.of(
			"yumi", "populate_system_details"
	);

	@Override
	public void onInitialize(ModContainer mod) {
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.addPhaseOrdering(
				CrashReportEvents.SYSTEM_DETAILS_POPULATE.defaultPhaseId(),
				SYSTEM_DETAILS_POPULATION_PHASE
		);
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.register(SYSTEM_DETAILS_POPULATION_PHASE, details -> {
			details.setDetail("Yumi MC Core", mod.getVersionString());

			if (YumiMods.get().getMod("fabric-crash-report-info-v1").isEmpty()
					&& YumiMods.get().getMod("neoforge").isEmpty()
			) {
				details.setDetail("Mods", () -> {
					var builder = new StringBuilder();
					this.populateMods(builder, 2,
							YumiMods.get().getMods().stream()
									.filter(entry -> entry.getContainingMod().isEmpty())
									.toList()
					);
					return builder.toString();
				});
			}
		});
	}

	private void populateMods(StringBuilder builder, int depth, Collection<ModContainer> mods) {
		for (var mod : mods.stream().sorted(Comparator.comparing(ModContainer::id)).toList()) {
			builder.append('\n');
			builder.append("\t".repeat(depth));
			builder.append(mod.id()).append(": ").append(mod.getName()).append(" v").append(mod.getVersionString());

			var contained = mod.getContainedMods();
			if (!contained.isEmpty()) {
				this.populateMods(builder, depth + 1, contained);
			}
		}
	}
}
