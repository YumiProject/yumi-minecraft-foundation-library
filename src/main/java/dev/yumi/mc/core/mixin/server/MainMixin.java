/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.server;

import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.server.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(Main.class)
class MainMixin {
	@Inject(
			method = "main",
			at = @At(value = "INVOKE", target = "Ljava/io/File;<init>(Ljava/lang/String;)V", ordinal = 0),
			remap = false
	)
	private static void onInit(String[] strings, CallbackInfo ci) {
		YumiMods.get().invokeEntrypoints(
				DedicatedServerModInitializer.ENTRYPOINT_KEY,
				DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeDedicatedServer
		);
	}
}