/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.neoforge;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NeoForgeMod.class)
public interface NeoForgeModAccessor {
	@Accessor
	static DeferredRegister<Attribute> getATTRIBUTES() {
		throw new IllegalStateException("Mixin injection failed!");
	}

	@Accessor
	static DeferredRegister<ArgumentTypeInfo<?, ?>> getCOMMAND_ARGUMENT_TYPES() {
		throw new IllegalStateException("Mixin injection failed!");
	}

	@Accessor
	static DeferredRegister<MapCodec<? extends EntitySubPredicate>> getENTITY_PREDICATE_CODECS() {
		throw new IllegalStateException("Mixin injection failed!");
	}

	@Accessor
	static DeferredRegister<ItemSubPredicate.Type<?>> getITEM_SUB_PREDICATES() {
		throw new IllegalStateException("Mixin injection failed!");
	}
}
