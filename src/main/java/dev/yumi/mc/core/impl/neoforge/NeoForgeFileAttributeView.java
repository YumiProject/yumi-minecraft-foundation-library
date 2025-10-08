/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.neoforge;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public record NeoForgeFileAttributeView(NeoForgePath path)
		implements BasicFileAttributeView {
	@Override
	public String name() {
		return "basic";
	}

	@Override
	public BasicFileAttributes readAttributes() throws IOException {
		return this.path.getFileSystem().provider().readBasicAttributes(this.path);
	}

	@Override
	public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) {
	}
}
