/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.mod;

import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.metadata.ManifestCustomValue;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@ApiStatus.Internal
public final class FabricModContainer extends AbstractModContainer {
	private final ModContainer fabric;

	public FabricModContainer(ModContainer fabric) {
		super(readCustomProperties(fabric.getMetadata()));
		this.fabric = fabric;
		this.readEntrypoints();
	}

	private static ManifestCustomValue.ObjectValue readCustomProperties(ModMetadata metadata) {
		record Entry(String key, ManifestCustomValue<?> value) {}
		Map<String, ManifestCustomValue<?>> map = metadata.getCustomValues().entrySet()
				.stream()
				.map(entry -> new Entry(entry.getKey(), mapCustomValue(entry.getValue())))
				.filter(entry -> entry.value != null)
				.collect(Collectors.toMap(Entry::key, Entry::value));

		return new ManifestCustomValue.ObjectValue(Map.copyOf(map));
	}

	@Override
	public @NotNull String id() {
		return this.fabric.getMetadata().getId();
	}

	@Override
	public @NotNull String getName() {
		return this.fabric.getMetadata().getName();
	}

	@Override
	public @NotNull String getVersionString() {
		return this.fabric.getMetadata().getVersion().toString();
	}

	@Override
	public @NotNull Optional<Path> findPath(String first, String... more) {
		var path = new StringBuilder(first);
		for (var part : more) {
			path.append('/').append(part);
		}

		return this.fabric.findPath(path.toString());
	}

	@Override
	public @NotNull Optional<dev.yumi.mc.core.api.ModContainer> getContainingMod() {
		return this.fabric.getContainingMod().flatMap(mod -> YumiMods.get().getMod(mod.getMetadata().getId()));
	}

	@Override
	public @Unmodifiable Collection<dev.yumi.mc.core.api.ModContainer> getContainedMods() {
		return this.fabric.getContainedMods().stream()
				.map(mod -> YumiMods.get().getMod(mod.getMetadata().getId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
	}

	@Override
	public @Unmodifiable Collection<String> getProvidedIds() {
		return this.fabric.getMetadata().getProvides();
	}

	public static void init(List<ExtendedModContainer> mods) {
		FabricLoader.getInstance().getAllMods().forEach(mod -> {
			var modContainer = new FabricModContainer(mod);
			mods.add(modContainer);
		});
	}

	private static ManifestCustomValue<?> mapCustomValue(CustomValue customValue) {
		return switch (customValue.getType()) {
			case OBJECT -> {
				var map = new HashMap<String, ManifestCustomValue<?>>();
				for (var entry : customValue.getAsObject()) {
					map.put(entry.getKey(), mapCustomValue(entry.getValue()));
				}
				yield new ManifestCustomValue.ObjectValue(Collections.unmodifiableMap(map));
			}
			case ARRAY -> {
				var list = new ArrayList<ManifestCustomValue<?>>();
				for (var entry : customValue.getAsArray()) {
					list.add(mapCustomValue(entry));
				}
				yield new ManifestCustomValue.ArrayValue(List.copyOf(list));
			}
			case STRING -> new ManifestCustomValue.StringValue(customValue.getAsString());
			case NUMBER -> new ManifestCustomValue.NumberValue(customValue.getAsNumber());
			case BOOLEAN -> new ManifestCustomValue.BooleanValue(customValue.getAsBoolean());
			case NULL -> null;
		};
	}
}
