package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class DeferredRegister<T> {
	private final Map<?, ?> entries = new LinkedHashMap<>();
	private final Map<Identifier, Identifier> aliases = new HashMap<>();
	private boolean seenRegisterEvent = false;

	public abstract Supplier<Registry<T>> getRegistry();

	public abstract ResourceKey<? extends Registry<T>> getRegistryKey();

	private void addEntries(RegisterEvent event) {}
}
