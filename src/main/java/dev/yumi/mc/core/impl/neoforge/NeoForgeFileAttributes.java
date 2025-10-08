/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.neoforge;

import net.neoforged.fml.jarcontents.JarResourceAttributes;
import org.jetbrains.annotations.Nullable;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public record NeoForgeFileAttributes(
		NeoForgeFileSystem.Entry entry,
		@Nullable JarResourceAttributes attributes
)
		implements BasicFileAttributes {
	@Override
	public FileTime lastModifiedTime() {
		return this.attributes != null ? this.attributes.lastModified() : FileTime.from(Instant.EPOCH);
	}

	@Override
	public FileTime lastAccessTime() {
		return this.lastModifiedTime();
	}

	@Override
	public FileTime creationTime() {
		return this.lastModifiedTime();
	}

	@Override
	public boolean isRegularFile() {
		return this.entry instanceof NeoForgeFileSystem.File;
	}

	@Override
	public boolean isDirectory() {
		return this.entry instanceof NeoForgeFileSystem.Directory;
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isOther() {
		return false;
	}

	@Override
	public long size() {
		return this.attributes != null ? this.attributes.size() : 0;
	}

	@Override
	public Object fileKey() {
		return null;
	}
}
