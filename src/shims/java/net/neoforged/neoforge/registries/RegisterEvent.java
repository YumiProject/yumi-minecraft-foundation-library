package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class RegisterEvent {
	RegisterEvent(ResourceKey<? extends Registry<?>> registryKey, Registry<?> registry) {}
}
