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
import net.minecraft.CrashReportCategory;
import net.minecraft.SystemReport;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Provides multiple crash-report-related events,
 * which allow to detect when a crash-report gets generated and to add custom debugging information.
 */
public final class CrashReportEvents {
	/**
	 * The crash report creation event.
	 * This event is invoked when a crash report is created.
	 *
	 * @see Creation
	 */
	public static final Event<Identifier, Creation> CREATE = YumiMods.EVENTS.create(Creation.class);

	/**
	 * The crash report system details population event.
	 * This event is invoked when the system details of a crash report are populated.
	 *
	 * @see SystemDetailsPopulation
	 * @see SystemReport
	 */
	public static final Event<Identifier, SystemDetailsPopulation> SYSTEM_DETAILS_POPULATE
			= YumiMods.EVENTS.create(SystemDetailsPopulation.class);

	/**
	 * The crash report entity details population event.
	 * This event is invoked when {@link Entity#fillCrashReportCategory(CrashReportCategory)} is called.
	 *
	 * @see EntityDetailsPopulation
	 */
	public static final Event<Identifier, EntityDetailsPopulation> ENTITY_DETAILS_POPULATE
			= YumiMods.EVENTS.create(EntityDetailsPopulation.class);

	/**
	 * The crash report block entity details population event.
	 * This event is invoked when {@link BlockEntity#fillCrashReportCategory(CrashReportCategory)} is called.
	 *
	 * @see BlockEntityDetailsPopulation
	 */
	public static final Event<Identifier, BlockEntityDetailsPopulation> BLOCK_ENTITY_DETAILS_POPULATE
			= YumiMods.EVENTS.create(BlockEntityDetailsPopulation.class);

	/**
	 * The crash report block details population event.
	 * This event is invoked when {@link CrashReportCategory#populateBlockDetails(CrashReportCategory, LevelHeightAccessor, BlockPos, BlockState)}
	 * is called.
	 *
	 * @see BlockDetailsPopulation
	 */
	public static final Event<Identifier, BlockDetailsPopulation> BLOCK_DETAILS_POPULATE
			= YumiMods.EVENTS.create(BlockDetailsPopulation.class);

	/**
	 * The crash report level details population event.
	 * This event is invoked when {@link Level#fillReportDetails(CrashReport)} is called.
	 *
	 * @see LevelDetailsPopulation
	 */
	public static final Event<Identifier, LevelDetailsPopulation> LEVEL_DETAILS_POPULATE
			= YumiMods.EVENTS.create(LevelDetailsPopulation.class);

	/**
	 * Represents the crash report creation callback interface.
	 *
	 * @see #CREATE
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
	 *
	 * @see #SYSTEM_DETAILS_POPULATE
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

	/**
	 * Represents the entity details population of crash reports callback interface.
	 *
	 * @see #ENTITY_DETAILS_POPULATE
	 */
	@FunctionalInterface
	public interface EntityDetailsPopulation extends EventAwareListener {
		/**
		 * Invoked during the population of a crash report's entity details.
		 *
		 * @param entity the entity whose details are gathered from
		 * @param category the crash report category
		 */
		void onCrashReportEntityDetailsPopulation(Entity entity, CrashReportCategory category);
	}

	/**
	 * Represents the block entity details population of crash reports callback interface.
	 *
	 * @see #BLOCK_ENTITY_DETAILS_POPULATE
	 */
	@FunctionalInterface
	public interface BlockEntityDetailsPopulation extends EventAwareListener {
		/**
		 * Invoked during the population of a crash report's block entity details.
		 *
		 * @param blockEntity the block entity whose details are gathered from
		 * @param category the crash report category
		 */
		void onCrashReportBlockEntityDetailsPopulation(BlockEntity blockEntity, CrashReportCategory category);
	}

	/**
	 * Represents the block details population of crash reports callback interface.
	 *
	 * @see #BLOCK_DETAILS_POPULATE
	 */
	@FunctionalInterface
	public interface BlockDetailsPopulation extends EventAwareListener {
		/**
		 * Invoked during the population of a crash report's block details.
		 *
		 * @param level the level the block is from
		 * @param pos the position of the block
		 * @param state the state of the block if gathered
		 * @param category the crash report category
		 */
		void onCrashReportBlockDetailsPopulation(
				LevelHeightAccessor level, BlockPos pos, @Nullable BlockState state, CrashReportCategory category
		);
	}

	/**
	 * Represents the level details population of crash reports callback interface.
	 *
	 * @see #LEVEL_DETAILS_POPULATE
	 */
	@FunctionalInterface
	public interface LevelDetailsPopulation extends EventAwareListener {
		/**
		 * Invoked during the population of a crash report's level details.
		 *
		 * @param level the level whose details are gathered from
		 * @param category the crash report category
		 */
		void onCrashReportLevelDetailsPopulation(Level level, CrashReportCategory category);
	}
}
