/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.mod;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.impl.entrypoint.EntrypointCandidate;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public interface ExtendedModContainer extends ModContainer {
	Map<String, List<EntrypointCandidate>> getEntrypoints();
}
