/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.entrypoint.client;

import dev.yumi.mc.core.api.ModContainer;
import org.jspecify.annotations.NullMarked;

/**
 * A mod initializer which is run only on {@link net.fabricmc.api.EnvType#CLIENT}.
 * <p>
 * This value is suitable for setting up client-specific logic, such as rendering
 * or integrated server tweaks.
 * <p>
 * In mod manifests, the value is defined with {@value #ENTRYPOINT_KEY} key.
 * <p>
 * Currently, it is executed in the {@link net.minecraft.client.Minecraft} constructor, just before the initialization of
 * the {@link net.minecraft.client.Options}.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 * @see dev.yumi.mc.core.api.entrypoint.ModInitializer
 * @see dev.yumi.mc.core.api.entrypoint.server.DedicatedServerModInitializer
 * @see dev.yumi.mc.core.api.entrypoint Entrypoints - Registering entrypoints
 */
@NullMarked
public interface ClientModInitializer {
	/**
	 * Represents the key which this value is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "yumi:client_init";

	/**
	 * Runs the client mod initializer.
	 *
	 * @param mod the mod which is initialized
	 */
	void onInitializeClient(ModContainer mod);
}
