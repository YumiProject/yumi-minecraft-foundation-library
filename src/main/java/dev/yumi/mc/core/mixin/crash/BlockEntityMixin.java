/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.crash;

import dev.yumi.mc.core.api.CrashReportEvents;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
class BlockEntityMixin {
	@Inject(method = "fillCrashReportCategory", at = @At("TAIL"))
	public void yumi$onPopulateCrashDetails(CrashReportCategory crashReportCategory, CallbackInfo ci) {
		try {
			CrashReportEvents.BLOCK_ENTITY_DETAILS_POPULATE.invoker()
					.onCrashReportBlockEntityDetailsPopulation((BlockEntity) (Object) this, crashReportCategory);
		} catch (Throwable e) {
			crashReportCategory.setDetailError("Block Entity Details Population Error", e);
		}
	}
}
