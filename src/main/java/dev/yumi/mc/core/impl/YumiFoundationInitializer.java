/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import dev.yumi.mc.core.api.CrashReportEvents;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.YumiEvents;
import dev.yumi.mc.core.api.YumiMods;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@ApiStatus.Internal
public final class YumiFoundationInitializer implements ModInitializer {
	private static final Identifier SYSTEM_DETAILS_POPULATION_PHASE = Identifier.of(
			"yumi", "populate_system_details"
	);

	@Override
	public void onInitialize(ModContainer mod) {
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

		CrashReportEvents.SYSTEM_DETAILS_POPULATE.addPhaseOrdering(
				CrashReportEvents.SYSTEM_DETAILS_POPULATE.defaultPhaseId(),
				SYSTEM_DETAILS_POPULATION_PHASE
		);
		CrashReportEvents.SYSTEM_DETAILS_POPULATE.register(SYSTEM_DETAILS_POPULATION_PHASE, details -> {
			details.setDetail("Yumi MC Core", mod.getVersionString());

			if (YumiMods.get().getMod("fabric-crash-report-info-v1").isEmpty()
					&& YumiMods.get().getMod("neoforge").isEmpty()
			) {
				details.setDetail("Mods", () -> {
					var builder = new StringBuilder();
					this.populateMods(builder, 2,
							YumiMods.get().getMods().stream()
									.filter(entry -> entry.getContainingMod().isEmpty())
									.toList()
					);
					return builder.toString();
				});
			}
		});
	}

	private void populateMods(StringBuilder builder, int depth, Collection<ModContainer> mods) {
		for (var mod : mods.stream().sorted(Comparator.comparing(ModContainer::id)).toList()) {
			builder.append('\n');
			builder.append("\t".repeat(depth));
			builder.append(mod.id()).append(": ").append(mod.getName()).append(" v").append(mod.getVersionString());

			var contained = mod.getContainedMods();
			if (!contained.isEmpty()) {
				this.populateMods(builder, depth + 1, contained);
			}
		}
	}
}
