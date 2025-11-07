/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.event.server;

import org.jspecify.annotations.NullMarked;

/**
 * Represents a dedicated-server-sided event callback aware of its uniquely associated event, may be used as an entrypoint.
 * <p>
 * In mod manifests, the entrypoint is defined with {@value ENTRYPOINT_KEY} key.
 * <p>
 * Any event callback interface extending this interface can be listened using this entrypoint.
 *
 * @see dev.yumi.mc.core.api.event.EventAwareListener
 * @see dev.yumi.mc.core.api.event.client.ClientEventAwareListener
 * @see dev.yumi.mc.core.api.entrypoint Entrypoints - Registering entrypoints
 */
@NullMarked
public interface DedicatedServerEventAwareListener {
	/**
	 * Represents the key which this value is defined with, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "yumi:dedicated_server_events";
}
