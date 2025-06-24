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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
class CrashReportMixin {
	@Unique
	private boolean yumi$firedEvent = false;

	@Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At(value = "HEAD"))
	void yumi$onCrashReportCreate(StringBuilder crashReportBuilder, CallbackInfo ci) {
		if (!this.yumi$firedEvent) {
			CrashReportEvents.CREATE.invoker().onCrashReportCreation((CrashReport) (Object) this);
			this.yumi$firedEvent = true;
		}
	}
}
