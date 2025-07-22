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
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "fillCrashReportCategory", at = @At("TAIL"))
	public void yumi$onPopulateCrashDetails(CrashReportCategory crashReportCategory, CallbackInfo ci) {
		CrashReportEvents.ENTITY_DETAILS_POPULATE.invoker()
				.onCrashReportEntityDetailsPopulation((Entity) (Object) this, crashReportCategory);
	}
}
