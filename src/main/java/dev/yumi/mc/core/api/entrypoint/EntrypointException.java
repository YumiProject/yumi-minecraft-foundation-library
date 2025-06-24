/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.entrypoint;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an exception that arises when obtaining entrypoints.
 *
 * @version 1.0.0
 * @since 1.0.0
 * @see dev.yumi.mc.core.api.YumiMods#getEntrypoints(String, Class)
 */
public class EntrypointException extends RuntimeException {
	private final String key;

	@ApiStatus.Internal
	public EntrypointException(String key, Throwable cause) {
		super("Exception while loading entries for entrypoint '" + key + "'!", cause);
		this.key = key;
	}

	@ApiStatus.Internal
	public EntrypointException(String key, String causingMod, Throwable cause) {
		super("Exception while loading entries for entrypoint '" + key + "' provided by '" + causingMod + "'!", cause);
		this.key = key;
	}

	/**
	 * {@return the entrypoint key which caused this exception}
	 */
	public @NotNull String key() {
		return this.key;
	}
}
