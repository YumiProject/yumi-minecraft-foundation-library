/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.neoforge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import org.jspecify.annotations.Nullable;

import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class NeoForgeFileSystem extends FileSystem {
	static final String URI_SCHEME = "yumi.virtual.neoforge";

	private static final String REGEX_SYNTAX = "regex";

	private final NeoForgeFileSystemProvider provider;
	final String name;
	final NeoForgePath root;
	final Directory rootEntries;

	public NeoForgeFileSystem(
			NeoForgeFileSystemProvider provider, String name, JarContents jarContents
	) {
		this.provider = provider;
		this.name = name;
		this.root = new NeoForgePath(this, null, NeoForgePath.ROOT);
		this.rootEntries = new Directory(NeoForgePath.ROOT, buildTree(jarContents));
	}

	@Override
	public NeoForgeFileSystemProvider provider() {
		return this.provider;
	}

	@Override
	public void close() {
		// Do nothing.
	}

	@Override
	public boolean isOpen() {
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public String getSeparator() {
		return "/";
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return List.of(this.root);
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		return List.of();
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return Set.of();
	}

	@Nullable Entry resolveEntry(NeoForgePath path) {
		if (path.getFileName() == null) {
			return null;
		} else if (path.isRoot()) {
			return this.rootEntries;
		}

		path = path.normalize().toAbsolutePath();
		var names = path.names();

		Entry entry = this.rootEntries.entries.get(names.getFirst());
		for (int i = 1; i < names.size(); i++) {
			if (entry instanceof Directory dir) {
				entry = dir.entries().get(names.get(i - 1));
			} else {
				return null;
			}
		}

		return entry;
	}

	NeoForgePath createPath(@Nullable NeoForgePath parent, String name) {
		return new NeoForgePath(this, parent, name);
	}

	@Override
	public Path getPath(String first, String... more) {
		if (first.isEmpty()) {
			return this.createPath(null, "");
		}

		NeoForgePath path;
		if (more.length == 0) {
			path = first.startsWith("/") ? this.root : null;
			for (String sub : first.split("/")) {
				if (path == null) {
					path = this.createPath(null, sub);
				} else {
					path = path.resolve(sub);
				}
			}
		} else {
			path = this.createPath(null, first);
			for (String sub : more) {
				path = path.resolve(sub);
			}
		}

		assert path != null;
		return path;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		int pos = syntaxAndPattern.indexOf(':');
		if (pos <= 0) {
			throw new IllegalArgumentException();
		}
		String syntax = syntaxAndPattern.substring(0, pos);
		String input = syntaxAndPattern.substring(pos + 1);
		String expr;
		if (syntax.equalsIgnoreCase(REGEX_SYNTAX)) {
			expr = input;
		} else {
			throw new UnsupportedOperationException("Syntax '" + syntax +
					"' not recognized");
		}
		// return matcher
		final Pattern pattern = Pattern.compile(expr);
		return (path) -> pattern.matcher(path.toString()).matches();
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchService newWatchService() {
		throw new UnsupportedOperationException("Watch service is not supported for NeoForge filesystems.");
	}

	static Map<String, Entry> buildTree(JarContents contents) {
		final Map<String, Entry> rootEntries = new Object2ObjectOpenHashMap<>();

		contents.visitContent((path, resource) -> {
			var parts = path.split("/");

			Map<String, Entry> entries = rootEntries;
			for (int i = 0; i < parts.length - 1; i++) {
				Directory entry = (Directory) entries.computeIfAbsent(
						parts[i],
						name -> new Directory(name, new Object2ObjectOpenHashMap<>())
				);
				entries = entry.entries;
			}

			entries.put(parts[parts.length - 1], new File(parts[parts.length - 1], resource.retain()));
		});

		return rootEntries;
	}

	sealed interface Entry {
		String name();
	}

	record Directory(String name, Map<String, Entry> entries) implements Entry {}

	record File(String name, JarResource resource) implements Entry {}
}
