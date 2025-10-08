/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.test;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

public class TestModInitializer implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("YumiMC|Test|ModInitializer");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Initializing test mod {}...", mod.getName());
		LOGGER.info("Is development? {}", YumiMods.get().isDevelopmentEnvironment());
		LOGGER.info("Game Directory: {}", YumiMods.get().getGameDirectory());
		LOGGER.info("Config Directory: {}", YumiMods.get().getConfigDirectory());

		var path = mod.findPath("fabric.mod.json")
				.orElseThrow(() -> new IllegalStateException("Could not find fabric.mod.json."));
		LOGGER.info("Found FMJ at {} (URI: {})", path, path.toUri());

		try (var files = Files.list(path.getParent())) {
			LOGGER.info("Root files: {}", files.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
