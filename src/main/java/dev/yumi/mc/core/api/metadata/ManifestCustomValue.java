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

import java.util.*;
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
			implements ManifestCustomValue<List<ManifestCustomValue<?>>>, SequencedCollection<ManifestCustomValue<?>> {
		@Override
		public int size() {
			return this.value.size();
		}

		@Override
		public boolean isEmpty() {
			return this.value.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return this.value.contains(o);
		}

		@Override
		public @NotNull Iterator<ManifestCustomValue<?>> iterator() {
			return this.value.iterator();
		}

		@Override
		public Object @NotNull [] toArray() {
			return this.value.toArray();
		}

		@Override
		public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
			return this.value.toArray(a);
		}

		@Override
		public boolean add(ManifestCustomValue<?> manifestCustomValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll(@NotNull Collection<?> c) {
			//noinspection SlowListContainsAll
			return this.value.containsAll(c);
		}

		@Override
		public boolean addAll(@NotNull Collection<? extends ManifestCustomValue<?>> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void forEach(Consumer<? super ManifestCustomValue<?>> action) {
			this.value.forEach(action);
		}

		@Override
		public @NotNull Spliterator<ManifestCustomValue<?>> spliterator() {
			return this.value.spliterator();
		}

		@Override
		public SequencedCollection<ManifestCustomValue<?>> reversed() {
			return this.value.reversed();
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
