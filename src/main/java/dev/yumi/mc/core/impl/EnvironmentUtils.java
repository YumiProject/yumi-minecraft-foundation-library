/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class EnvironmentUtils {
	/**
	 * {@code true} if this environment has Fabric, or {@code false} otherwise
	 */
	public static final boolean FABRIC = isFabric();

	/**
	 * {@code true} if this environment has NeoForge, or {@code false} otherwise
	 */
	public static final boolean NEOFORGE = isNeoForge();

	private EnvironmentUtils() {
		throw new UnsupportedOperationException("EnvironmentUtils only contains static definitions.");
	}

	private static boolean isFabric() {
		try {
			Class.forName("net.fabricmc.loader.api.FabricLoader");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static boolean isNeoForge() {
		try {
			Class.forName("net.neoforged.fml.loading.FMLLoader");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
