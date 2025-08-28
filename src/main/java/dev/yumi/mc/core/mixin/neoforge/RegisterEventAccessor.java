/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegisterEvent.class)
public interface RegisterEventAccessor {
	@Invoker("<init>")
	static RegisterEvent yumi$init(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
