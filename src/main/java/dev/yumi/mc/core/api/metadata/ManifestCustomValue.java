/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public sealed interface ManifestCustomValue<T> {
	@NotNull T value();

	record ObjectValue(@Unmodifiable Map<String, ManifestCustomValue<?>> value)
			implements ManifestCustomValue<Map<String, ManifestCustomValue<?>>> {
		public boolean isEmpty() {
			return this.value.isEmpty();
		}

		public ManifestCustomValue<?> get(@NotNull String key) {
			return this.value.get(key);
		}
	}

	record ArrayValue(@Unmodifiable List<ManifestCustomValue<?>> value)
			implements ManifestCustomValue<List<ManifestCustomValue<?>>>, Iterable<ManifestCustomValue<?>> {
		@Override
		public @NotNull Iterator<ManifestCustomValue<?>> iterator() {
			return this.value.iterator();
		}

		@Override
		public void forEach(Consumer<? super ManifestCustomValue<?>> action) {
			this.value.forEach(action);
		}

		@Override
		public @NotNull Spliterator<ManifestCustomValue<?>> spliterator() {
			return this.value.spliterator();
		}
	}

	record StringValue(String value) implements ManifestCustomValue<String> {
	}

	record BooleanValue(boolean bool) implements ManifestCustomValue<Boolean> {
		@Override
		public @NotNull Boolean value() {
			return this.bool;
		}
	}

	record NumberValue(Number value) implements ManifestCustomValue<Number> {}
}
