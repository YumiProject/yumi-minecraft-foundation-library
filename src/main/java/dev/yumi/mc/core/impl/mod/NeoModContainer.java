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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	public @NotNull String id() {
		return this.modInfo.getModId();
	}

	@Override
	public @NotNull String getName() {
		return this.modInfo.getDisplayName();
	}

	@Override
	public @NotNull String getVersionString() {
		return this.modInfo.getVersion().toString();
	}

	@Override
	public @NotNull Optional<Path> findPath(String first, String... more) {
		var path = this.fileSystem.get().getPath(first, more).toAbsolutePath();

		if (Files.exists(path)) {
			return Optional.of(path);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public @NotNull Optional<ModContainer> getContainingMod() {
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
	private static ManifestCustomValue<?> mapCustomValue(Object value) {
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
		record Entry(String key, ManifestCustomValue<?> value) {}
		Map<String, ManifestCustomValue<?>> map = value.entrySet().stream()
				.map(entry -> new Entry(entry.getKey(), mapCustomValue(entry.getValue())))
				.filter(entry -> entry.value != null)
				.collect(Collectors.toMap(Entry::key, Entry::value));

		return new ManifestCustomValue.ObjectValue(Collections.unmodifiableMap(map));
	}
}
