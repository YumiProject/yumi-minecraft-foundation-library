/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.entrypoint;

import dev.yumi.mc.core.api.ModContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the container of an entrypoint.
 *
 * @param mod the mod who supplies this entrypoint
 * @param value the entrypoint
 * @param <T> the type of this entrypoint
 * @version 1.0.0
 * @since 1.0.0
 */
public record EntrypointContainer<T>(
		@NotNull ModContainer mod,
		@NotNull T value
) {
}
