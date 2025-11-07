/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import com.mojang.logging.LogUtils;
import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiEvents;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import net.minecraft.SystemReport;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

@ApiStatus.Internal
public final class YumiFoundationMod implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Identifier SYSTEM_DETAILS_POPULATION_PHASE = Identifier.fromNamespaceAndPath(
			"yumi", "populate_system_details"
	);

	@Override
	public void onInitialize(ModContainer mod) {
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.addPhaseOrdering(
				CrashReportEvents.SYSTEM_DETAILS_POPULATE.defaultPhaseId(),
				SYSTEM_DETAILS_POPULATION_PHASE
		);
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.register(SYSTEM_DETAILS_POPULATION_PHASE, details ->
				details.setDetail("Yumi MC Core", mod.getVersionString())
		);
	}

	public static void initialize() {
		LOGGER.info("Initializing mods (entrypoint {})...", ModInitializer.ENTRYPOINT_KEY);
		YumiModsImpl.INSTANCE.runtimes.forEach(CurrentRuntime::init);

		YumiEvents.EVENTS.getCreationEvent().register((manager, event) -> {
			for (var target : EventSideTarget.VALUES) {
				// Search if the callback qualifies is unique to this event.
				if (target.listenerClass().isAssignableFrom(event.type())) {
					List<?> entrypoints = YumiMods.get().getEntrypoints(target.entrypointKey(), target.listenerClass());

					// Search for matching entrypoint.
					for (Object entrypoint : entrypoints) {
						// Searching if the given entrypoint is a listener of the event being registered.
						if (event.type().isAssignableFrom(entrypoint.getClass())) {
							// It is, then register the listener.
							manager.listenAll(entrypoint, event);
						}
					}

					break;
				}
			}
		});

		YumiModsImpl.INSTANCE.invokeEntrypoints(ModInitializer.ENTRYPOINT_KEY, ModInitializer.class, ModInitializer::onInitialize);
	}

	public static void populateSystemDetailsReport(SystemReport details) {
		if (YumiMods.get().getMod("fabric-crash-report-info-v1").isEmpty()) {
			details.setDetail("Mods", () -> {
				var builder = new StringBuilder();
				populateMods(builder, 2,
						YumiMods.get().getMods().stream()
								.filter(entry -> entry.getContainingMod().isEmpty())
								.toList(),
						new Stack<>()
				);
				return builder.toString();
			});
		}
	}

	private static void populateMods(
			StringBuilder builder, int depth, Collection<ModContainer> mods, Stack<ModContainer> parents
	) {
		for (var mod : mods.stream().sorted(Comparator.comparing(ModContainer::id)).toList()) {
			builder.append('\n');
			builder.append("\t".repeat(depth));
			builder.append(mod.id()).append(": ").append(mod.getName()).append(" v").append(mod.getVersionString());

			if (!parents.contains(mod)) {
				parents.push(mod);
				var contained = mod.getContainedMods();
				if (!contained.isEmpty()) {
					populateMods(builder, depth + 1, contained, parents);
				}
				parents.pop();
			}
		}
	}
}
