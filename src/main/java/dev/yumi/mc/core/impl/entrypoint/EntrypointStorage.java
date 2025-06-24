/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.entrypoint;

import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public sealed interface EntrypointStorage permits CommonEntrypointStorage, FabricEntrypointStorage, JoinedEntrypointStorage {
	<T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type);
}
