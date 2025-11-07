/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api;

import dev.yumi.mc.core.api.metadata.ManifestCustomValue;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents a mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ModContainer {
	/**
	 * {@return the identifier of this mod}
	 */
	String id();

	/**
	 * {@return the name of this mod}
	 */
	String getName();

	/**
	 * {@return the string representation of the version of this mod}
	 */
	String getVersionString();

	/**
	 * Finds a resource path inside this mod.
	 *
	 * @param first the path string or initial part of the path string
	 * @param more additional strings to be joined to form the path string
	 * @return the path if it exists, or {@link Optional#empty()} otherwise
	 */
	Optional<Path> findPath(String first, String... more);

	/**
	 * {@return the mod which contains this mod, if any}
	 */
	Optional<ModContainer> getContainingMod();

	/**
	 * {@return the collection of mods contained within this mod}
	 */
	@Unmodifiable
	Collection<ModContainer> getContainedMods();

	/**
	 * {@return a collection of mod identifiers provided by this mod}
	 */
	@Unmodifiable
	Collection<String> getProvidedIds();

	/**
	 * {@return the custom properties of this mod}
	 */
	ManifestCustomValue.ObjectValue getCustomProperties();
}
