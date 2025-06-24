/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.mc.core.api.event.EventAwareListener;
import dev.yumi.mc.core.api.event.client.ClientEventAwareListener;
import dev.yumi.mc.core.api.event.server.DedicatedServerEventAwareListener;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
enum EventSideTarget {
	CLIENT(ClientEventAwareListener.ENTRYPOINT_KEY, ClientEventAwareListener.class),
	COMMON(EventAwareListener.ENTRYPOINT_KEY, EventAwareListener.class),
	DEDICATED_SERVER(DedicatedServerEventAwareListener.ENTRYPOINT_KEY, DedicatedServerEventAwareListener.class);

	public static final List<EventSideTarget> VALUES = List.of(values());

	private final String entrypointKey;
	private final Class<?> listenerClass;

	EventSideTarget(String entrypointKey, Class<?> listenerClass) {
		this.entrypointKey = entrypointKey;
		this.listenerClass = listenerClass;
	}

	public String entrypointKey() {
		return this.entrypointKey;
	}

	public Class<?> listenerClass() {
		return this.listenerClass;
	}
}
