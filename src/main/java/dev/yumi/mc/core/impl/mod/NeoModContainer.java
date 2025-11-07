/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.mod;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Suppliers;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.metadata.ManifestCustomValue;
import dev.yumi.mc.core.impl.neoforge.NeoForgeFileSystem;
import dev.yumi.mc.core.impl.neoforge.NeoForgeFileSystemProvider;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class NeoModContainer extends AbstractModContainer {
	private final IModInfo modInfo;
	private final Supplier<NeoForgeFileSystem> fileSystem;

	public NeoModContainer(IModInfo modInfo) {
		super(mapObjectCustomValue(modInfo.getModProperties()));
		this.modInfo = modInfo;
		this.readEntrypoints();
		this.fileSystem = Suppliers.memoize(() -> new NeoForgeFileSystem(
				NeoForgeFileSystemProvider.INSTANCE,
				this.id(),
				modInfo.getOwningFile().getFile().getContents()
		));
	}

	@Override
	public String id() {
		return this.modInfo.getModId();
	}

	@Override
	public String getName() {
		return this.modInfo.getDisplayName();
	}

	@Override
	public String getVersionString() {
		return this.modInfo.getVersion().toString();
	}

	@Override
	public Optional<Path> findPath(String first, String... more) {
		var path = this.fileSystem.get().getPath(first, more).toAbsolutePath();

		if (Files.exists(path)) {
			return Optional.of(path);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<ModContainer> getContainingMod() {
		return Optional.empty();
	}

	@Override
	public @Unmodifiable Collection<ModContainer> getContainedMods() {
		return List.of();
	}

	@Override
	public @Unmodifiable Collection<String> getProvidedIds() {
		return List.of();
	}

	public static void init(List<ExtendedModContainer> mods) {
		// Do not use ModList as early initialization will fail.
		// Apparently LoadingModList is always constructed fully populated and ModList only gets a copy of the same data.
		FMLLoader.getCurrent().getLoadingModList()
				.getMods()
				.forEach(mod -> mods.add(new NeoModContainer(mod)));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static @Nullable ManifestCustomValue<?> mapCustomValue(Object value) {
		return switch (value) {
			case Config object -> mapObjectCustomValue(object.valueMap());
			case Map map -> mapObjectCustomValue(map);
			case List list -> new ManifestCustomValue.ArrayValue(list.stream().map(NeoModContainer::mapCustomValue).toList());
			case String string -> new ManifestCustomValue.StringValue(string);
			case Number number -> new ManifestCustomValue.NumberValue(number);
			case Boolean bool -> new ManifestCustomValue.BooleanValue(bool);
			default -> null;
		};
	}

	private static ManifestCustomValue.ObjectValue mapObjectCustomValue(Map<String, Object> value) {
		var map = new HashMap<String, ManifestCustomValue<?>>();

		for (var entry : value.entrySet()) {
			var mappedValue = mapCustomValue(entry.getValue());

			if (mappedValue != null) {
				map.put(entry.getKey(), mappedValue);
			}
		}

		return new ManifestCustomValue.ObjectValue(Map.copyOf(map));
	}
}
