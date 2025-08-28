/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.mixin.neoforge;

import dev.yumi.mc.core.impl.neoforge.DeferredRegisterUndeferrer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(DeferredRegister.class)
public abstract class DeferredRegisterMixin implements DeferredRegisterUndeferrer {
	@Final
	@Shadow
	private Map<?, ?> entries;

	@Final
	@Shadow
	private Map<Identifier, Identifier> aliases;

	@Shadow
	private boolean seenRegisterEvent;

	@Shadow
	protected abstract void addEntries(RegisterEvent event);

	@SuppressWarnings({"DataFlowIssue"})
	@Override
	public void yumi$registerNow() {
		var $this = (DeferredRegister<?>) (Object) this;
		this.addEntries(RegisterEventAccessor.yumi$init($this.getRegistryKey(), $this.getRegistry().get()));
		this.seenRegisterEvent = false;
		this.entries.clear();
		this.aliases.clear();
	}
}
