/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.test.client;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class TestClientModInitializer implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("YumiMC|Test|ClientModInitializer");

	@Override
	public void onInitializeClient(ModContainer mod) {
		LOGGER.info("Initializing test mod {}", mod.getName());
	}
}
