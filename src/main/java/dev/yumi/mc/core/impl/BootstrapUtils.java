/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl;

import net.minecraft.server.Bootstrap;
import net.minecraft.server.DebugLoggedPrintStream;
import net.minecraft.server.LoggedPrintStream;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.io.PrintStream;

@ApiStatus.Internal
public final class BootstrapUtils {
	public static void wrapStreams(Logger logger, PrintStream stderr) {
		if (logger.isDebugEnabled()) {
			System.setErr(new DebugLoggedPrintStream("STDERR", stderr));
			System.setOut(new DebugLoggedPrintStream("STDOUT", Bootstrap.STDOUT));
		} else {
			System.setErr(new LoggedPrintStream("STDERR", stderr));
			System.setOut(new LoggedPrintStream("STDOUT", Bootstrap.STDOUT));
		}
	}
}
