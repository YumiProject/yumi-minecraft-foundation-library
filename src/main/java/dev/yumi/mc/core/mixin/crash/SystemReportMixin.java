/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.crash;

import dev.yumi.mc.core.api.CrashReportEvents;
import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SystemReport.class)
class SystemReportMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void yumi$onInit(CallbackInfo ci) {
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.invoker().onCrashReportSystemDetailsPopulation((SystemReport) (Object) this);
	}
}
