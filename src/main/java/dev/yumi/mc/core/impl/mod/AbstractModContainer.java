/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.mod;

import com.mojang.logging.LogUtils;
import dev.yumi.mc.core.api.metadata.ManifestCustomValue;
import dev.yumi.mc.core.impl.entrypoint.EntrypointCandidate;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
abstract class AbstractModContainer implements ExtendedModContainer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, List<EntrypointCandidate>> entrypoints = new HashMap<>();
	private final ManifestCustomValue.ObjectValue customProperties;

	protected AbstractModContainer(ManifestCustomValue.ObjectValue customProperties) {
		this.customProperties = customProperties;
	}

	protected void readEntrypoints() {
		var value = this.customProperties.get("yumi:entrypoints");

		if (value == null) return;
		if ((!(value instanceof ManifestCustomValue.ObjectValue(var map)))) {
			LOGGER.warn(
					"Yumi-managed entrypoints for mod {} cannot be read: `yumi:entrypoints` custom value is not an object.",
					this.id()
			);
			return;
		}

		map.forEach(this::readEntrypoints);
	}

	private void readEntrypoints(String key, @Nullable ManifestCustomValue<?> data) {
		if (data instanceof ManifestCustomValue.ArrayValue(var rawEntrypoints)) {
			this.entrypoints.put(key,
					rawEntrypoints.stream()
							.<EntrypointCandidate>mapMulti((entry, consumer) -> {
								var candidate = this.readEntrypoint(key, entry);
								if (candidate != null) {
									consumer.accept(candidate);
								}
							})
							.toList()
			);
		} else {
			var entrypoint = this.readEntrypoint(key, data);
			if (entrypoint != null) {
				this.entrypoints.put(key, List.of(entrypoint));
			}
		}
	}

	private @Nullable EntrypointCandidate readEntrypoint(String key, @Nullable ManifestCustomValue<?> data) {
		return switch (data) {
			case ManifestCustomValue.ObjectValue object -> {
				var value = object.get("value");
				if (value == null) {
					LOGGER.warn("Yumi-managed entrypoint {} for mod {} is missing `value` entry in object.", key, this.id());
					yield null;
				}
				if (!(value instanceof ManifestCustomValue.StringValue(var string))) {
					LOGGER.warn("Yumi-managed entrypoint {} for mod {} has a non-string `value` entry.", key, this.id());
					yield null;
				}
				yield new EntrypointCandidate(string);
			}
			case ManifestCustomValue.StringValue(var string) -> new EntrypointCandidate(string);
			case null, default -> {
				LOGGER.warn("Yumi-managed entrypoint {} of mod {} is not valid.", key, this.id());
				yield null;
			}
		};
	}

	@Override
	public ManifestCustomValue.ObjectValue getCustomProperties() {
		return this.customProperties;
	}

	@Override
	public Map<String, List<EntrypointCandidate>> getEntrypoints() {
		return this.entrypoints;
	}
}
