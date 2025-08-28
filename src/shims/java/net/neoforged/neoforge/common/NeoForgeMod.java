package net.neoforged.neoforge.common;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeMod {
	private static final DeferredRegister<Attribute> ATTRIBUTES = null;
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = null;
	private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_PREDICATE_CODECS = null;
	private static final DeferredRegister<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATES = null;
}
