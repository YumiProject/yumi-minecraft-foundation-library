/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.entrypoint;

import dev.yumi.mc.core.api.ModContainer;

/**
 * A mod initializer.
 * <p>
 * In mod manifests, the value is defined with the {@value #ENTRYPOINT_KEY} key.
 * <p>
 * Currently, it is executed in {@link net.minecraft.server.Bootstrap#bootStrap()}, just before the freezing of built-in registries.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 * @see dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer
 * @see dev.yumi.mc.core.api.entrypoint.server.DedicatedServerModInitializer
 * @see dev.yumi.mc.core.api.entrypoint Entrypoints - Registering entrypoints
 */
public interface ModInitializer {
	/**
	 * Represents the key which this value is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "yumi:init";

	/**
	 * Runs the mod initializer.
	 *
	 * @param mod the mod which is initialized
	 */
	void onInitialize(ModContainer mod);
}
