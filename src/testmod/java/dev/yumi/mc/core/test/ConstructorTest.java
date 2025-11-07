/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.test;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructorTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("YumiMC|Test|ConstructorTest");
	private final ModContainer mod;

	public ConstructorTest(ModContainer mod) {
		this.mod = mod;
	}

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Initializing test mod {}...", this.mod.getName());
		assert this.mod == mod;
	}
}
