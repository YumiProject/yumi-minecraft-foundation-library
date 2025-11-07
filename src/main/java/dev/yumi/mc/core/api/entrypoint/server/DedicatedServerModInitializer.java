/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.entrypoint.server;

import dev.yumi.mc.core.api.ModContainer;
import org.jspecify.annotations.NullMarked;

/**
 * A mod initializer which is run only on {@link net.fabricmc.api.EnvType#SERVER}.
 * <p>
 * In mod manifests, the value is defined with {@value #ENTRYPOINT_KEY} key.
 * <p>
 * Currently, it is executed in {@link net.minecraft.server.Main#main(String[])}, just after the EULA has been agreed to.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 * @see dev.yumi.mc.core.api.entrypoint.ModInitializer
 * @see dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer
 * @see dev.yumi.mc.core.api.entrypoint Entrypoints - Registering entrypoints
 */
@NullMarked
public interface DedicatedServerModInitializer {
	/**
	 * Represents the key which this value is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "yumi:dedicated_server_init";

	/**
	 * Runs the dedicated server mod initializer.
	 *
	 * @param mod the mod which is initialized
	 */
	void onInitializeDedicatedServer(ModContainer mod);
}
