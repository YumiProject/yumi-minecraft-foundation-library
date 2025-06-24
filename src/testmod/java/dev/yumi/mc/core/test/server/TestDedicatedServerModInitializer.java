/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.test.server;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.server.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.SERVER)
public class TestDedicatedServerModInitializer implements DedicatedServerModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("YumiMC|Test|DedicatedServerModInitializer");

	@Override
	public void onInitializeDedicatedServer(ModContainer mod) {
		LOGGER.info("Initializing test mod {}", mod.getName());
	}
}
