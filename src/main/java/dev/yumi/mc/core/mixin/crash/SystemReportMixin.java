/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.crash;

import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.impl.YumiFoundationMod;
import net.minecraft.SystemReport;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SystemReport.class)
class SystemReportMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void yumi$onInit(CallbackInfo ci) {
		var $this = (SystemReport) (Object) this;
		try {
			CrashReportEvents.SYSTEM_DETAILS_POPULATE.invoker().onCrashReportSystemDetailsPopulation($this);
			YumiFoundationMod.populateSystemDetailsReport($this);
		} catch (Throwable e) {
			LOGGER.error("[Yumi MC Foundation] Failed to populate some parts of the crash report.", e);
		}
	}
}
