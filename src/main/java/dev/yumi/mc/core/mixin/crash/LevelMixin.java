/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.crash;

import dev.yumi.mc.core.api.CrashReportEvents;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
class LevelMixin {
	@Inject(method = "fillReportDetails", at = @At("TAIL"))
	public void yumi$onPopulateCrashDetails(CrashReport crashReport, CallbackInfoReturnable<CrashReportCategory> cir) {
		try {
			CrashReportEvents.LEVEL_DETAILS_POPULATE.invoker()
					.onCrashReportLevelDetailsPopulation((Level) (Object) this, cir.getReturnValue());
		} catch (Throwable e) {
			crashReport.getException().addSuppressed(e);
		}
	}
}
