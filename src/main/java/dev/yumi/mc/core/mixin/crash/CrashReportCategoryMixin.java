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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReportCategory.class)
public class CrashReportCategoryMixin {
	@Inject(method = "populateBlockDetails", at = @At("TAIL"))
	private static void yumi$onPopulateBlockCrashDetails(
			CrashReportCategory crashReportCategory, LevelHeightAccessor level, BlockPos pos, @Nullable BlockState state, CallbackInfo ci
	) {
		CrashReportEvents.BLOCK_DETAILS_POPULATE.invoker()
				.onCrashReportBlockDetailsPopulation(level, pos, state, crashReportCategory);
	}
}
