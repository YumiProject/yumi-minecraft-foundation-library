/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.yumi.mc.core.impl.BootstrapUtils;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintStream;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
	@Shadow
	@Final
	private static Logger LOGGER;
	@Unique
	private static final PrintStream STDERR = System.err;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void yumi$onStaticInit(CallbackInfo ci) {
		// We need to make this a bit early in case a mod uses System.out to print stuff.
		BootstrapUtils.wrapStreams(LOGGER, STDERR);
	}

	@WrapOperation(
			method = "wrapStreams",
			at = @At(value = "INVOKE", target = "Ljava/lang/System;setOut(Ljava/io/PrintStream;)V")
	)
	private static void yumi$cancelStreamWrappingOut(PrintStream out, Operation<Void> original) { /* No op */ }

	@WrapOperation(
			method = "wrapStreams",
			at = @At(value = "INVOKE", target = "Ljava/lang/System;setErr(Ljava/io/PrintStream;)V")
	)
	private static void yumi$cancelStreamWrappingErr(PrintStream out, Operation<Void> original) { /* No op */ }
}
