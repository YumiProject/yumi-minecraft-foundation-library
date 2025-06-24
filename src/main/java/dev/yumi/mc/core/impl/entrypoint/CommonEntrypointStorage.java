/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *   Copyright 2016, 2017, 2018, 2019 FabricMC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.yumi.mc.core.impl.entrypoint;

import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.EntrypointContainer;
import dev.yumi.mc.core.api.entrypoint.EntrypointException;
import dev.yumi.mc.core.impl.mod.ExtendedModContainer;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
final class CommonEntrypointStorage implements EntrypointStorage {
	private final Map<String, List<Entry>> entrypoints = new HashMap<>();

	public CommonEntrypointStorage(List<ExtendedModContainer> mods) {
		for (var mod : mods) {
			for (var entry : mod.getEntrypoints().entrySet()) {
				this.collectEntrypointsForKey(mod, entry.getKey(), entry.getValue());
			}
		}
	}

	private void collectEntrypointsForKey(ModContainer mod, String key, List<EntrypointCandidate> candidates) {
		var storage = this.entrypoints.computeIfAbsent(key, k -> new ArrayList<>());
		for (var candidate : candidates) {
			storage.add(new Entry(mod, candidate));
		}
	}

	@Override
	public <T> List<EntrypointContainer<T>> getEntrypoints(String key, Class<T> type) {
		var containers = new ArrayList<EntrypointContainer<T>>();
		var entrypoints = this.entrypoints.getOrDefault(key, List.of());
		EntrypointException error = null;

		for (var entry : entrypoints) {
			try {
				containers.add(new EntrypointContainer<>(entry.mod, entry.resolve(key, type)));
			} catch (IllegalArgumentException e) {
				var newError = new EntrypointException(key, entry.mod.id(), e);

				if (error == null) {
					error = newError;
				} else {
					error.addSuppressed(newError);
				}
			}
		}

		if (error != null) {
			throw error;
		}

		return containers;
	}

	private record Entry(ModContainer mod, EntrypointCandidate entrypoint) {
		@SuppressWarnings("unchecked")
		public <T> T resolve(String key, Class<T> type) {
			String value = this.entrypoint.value();
			String[] methodSplit = value.split("::");

			if (methodSplit.length >= 3) {
				throw new IllegalArgumentException("Invalid handle format: " + value);
			}

			Class<?> c;

			try {
				c = Class.forName(methodSplit[0]);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}

			if (methodSplit.length == 1) {
				if (type.isAssignableFrom(c)) {
					try {
						return (T) c.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						throw new IllegalArgumentException(e);
					}
				} else {
					throw new IllegalArgumentException("Class " + c.getName() + " cannot be cast to " + type.getName() + "!");
				}
			} else /* length == 2 */ {
				List<Method> methodList = new ArrayList<>();

				for (Method m : c.getDeclaredMethods()) {
					if (!(m.getName().equals(methodSplit[1]))) {
						continue;
					}

					methodList.add(m);
				}

				try {
					Field field = c.getDeclaredField(methodSplit[1]);
					Class<?> fType = field.getType();

					if ((field.getModifiers() & Modifier.STATIC) == 0) {
						throw new IllegalArgumentException("Field " + value + " must be static!");
					}

					if (!methodList.isEmpty()) {
						throw new IllegalArgumentException("Ambiguous " + value + " - refers to both field and method!");
					}

					if (!type.isAssignableFrom(fType)) {
						throw new IllegalArgumentException("Field " + value + " cannot be cast to " + type.getName() + "!");
					}

					return (T) field.get(null);
				} catch (NoSuchFieldException e) {
					// ignore
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("Field " + value + " cannot be accessed!", e);
				}

				if (!type.isInterface()) {
					throw new IllegalArgumentException("Cannot proxy method " + value + " to non-interface type " + type.getName() + "!");
				}

				if (methodList.isEmpty()) {
					throw new IllegalArgumentException("Could not find " + value + "!");
				} else if (methodList.size() >= 2) {
					throw new IllegalArgumentException("Found multiple method entries of name " + value + "!");
				}

				final Method targetMethod = methodList.get(0);
				Object object = null;

				if ((targetMethod.getModifiers() & Modifier.STATIC) == 0) {
					try {
						object = c.getDeclaredConstructor().newInstance();
					} catch (Exception e) {
						throw new IllegalArgumentException(e);
					}
				}

				MethodHandle handle;

				try {
					handle = MethodHandles.lookup()
							.unreflect(targetMethod);
				} catch (Exception ex) {
					throw new IllegalArgumentException(ex);
				}

				if (object != null) {
					handle = handle.bindTo(object);
				}

				// uses proxy as well, but this handles default and object methods
				try {
					return MethodHandleProxies.asInterfaceInstance(type, handle);
				} catch (Exception ex) {
					throw new IllegalArgumentException(ex);
				}
			}
		}
	}
}
