/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.api;

import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.event.EventAwareListener;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.resources.Identifier;

public final class CrashReportEvents {
	public static final Event<Identifier, Creation> CREATE = YumiMods.EVENTS.create(Creation.class);

	public static final Event<Identifier, SystemDetailsPopulation> SYSTEM_DETAILS_POPULATE
			= YumiMods.EVENTS.create(SystemDetailsPopulation.class);

	/**
	 * Represents the crash report creation callback interface.
	 */
	@FunctionalInterface
	public interface Creation extends EventAwareListener {
		/**
		 * Invoked when the given crash report is created.
		 *
		 * @param report the crash report
		 */
		void onCrashReportCreation(CrashReport report);
	}

	/**
	 * Represents the system details population of crash reports callback interface.
	 */
	@FunctionalInterface
	public interface SystemDetailsPopulation extends EventAwareListener {
		/**
		 * Invoked during the population of a crash report's system details.
		 *
		 * @param report the system details
		 */
		void onCrashReportSystemDetailsPopulation(SystemReport report);
	}
}
