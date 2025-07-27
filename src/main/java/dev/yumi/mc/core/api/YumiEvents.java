/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api;

import dev.yumi.commons.event.EventManager;
import net.minecraft.resources.Identifier;

/**
 * Provides the Yumi event manager.
 */
public final class YumiEvents {
	/**
	 * Represents the Yumi event manager.
	 *
	 * @see dev.yumi.commons.event
	 */
	public static final EventManager<Identifier> EVENTS = new EventManager<>(
			Identifier.of("yumi", "default"),
			Identifier::parse
	);

	private YumiEvents() {
		throw new UnsupportedOperationException("YumiEvents only contains static definitions.");
	}
}
