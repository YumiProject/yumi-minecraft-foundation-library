/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.yumi.mc.core.impl.neoforge;

import dev.yumi.commons.function.YumiPredicates;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NeoForgeFileSystemProvider extends FileSystemProvider {
	public static final NeoForgeFileSystemProvider INSTANCE = new NeoForgeFileSystemProvider();

	@Override
	public String getScheme() {
		return NeoForgeFileSystem.URI_SCHEME;
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
		throw new UnsupportedOperationException("Only direct creation is supported.");
	}

	@Override
	public FileSystem getFileSystem(URI uri) {
		throw new UnsupportedOperationException("Only direct creation is supported.");
	}

	@Override
	public @NotNull Path getPath(@NotNull URI uri) {
		throw new UnsupportedOperationException(
				"This method should not be called directly; use Path.of(URI) instead."
		);
	}

	@Override
	public boolean exists(Path path, LinkOption... options) {
		var actualPath = (NeoForgePath) path;
		return actualPath.getFileSystem().resolveEntry(actualPath) != null;
	}

	@Override
	public InputStream newInputStream(Path path, OpenOption... options) throws IOException {
		var actualPath = (NeoForgePath) path;
		var entry = actualPath.getFileSystem().resolveEntry(actualPath);

		return switch (entry) {
			case NeoForgeFileSystem.File file -> file.resource().open();
			case NeoForgeFileSystem.Directory dir -> throw new FileNotFoundException("Cannot find file: " + actualPath);
			case null -> throw new FileNotFoundException("Cannot find file: " + actualPath);
		};
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		if (options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND)) {
			this.checkWritable();
		}

		var actualPath = (NeoForgePath) path;
		var entry = actualPath.getFileSystem().resolveEntry(actualPath);

		return switch (entry) {
			case NeoForgeFileSystem.File file -> new SeekableInMemoryByteChannel(file.resource().readAllBytes());
			case NeoForgeFileSystem.Directory dir -> throw new FileNotFoundException("Cannot find file: " + actualPath);
			case null -> throw new FileNotFoundException("Cannot find file: " + actualPath);
		};
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, @Nullable DirectoryStream.Filter<? super Path> filter)
			throws IOException {
		var path = (NeoForgePath) dir;

		if (!(path.getFileSystem().resolveEntry(path) instanceof NeoForgeFileSystem.Directory directory)) {
			throw new NotDirectoryException(path.toString());
		}

		return new DirectoryStream<>() {
			@Override
			public @NotNull Iterator<Path> iterator() {
				var stream = directory.entries().values().stream()
						.map(NeoForgeFileSystem.Entry::name)
						.map(dir::resolve);

				if (filter != null) {
					stream = stream.filter(path -> {
						try {
							return filter.accept(path);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				}

				return stream.iterator();
			}

			@Override
			public void close() {
			}
		};
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) {
		this.checkWritable();
	}

	@Override
	public void delete(Path path) {
		this.checkWritable();
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		throw new UnsupportedOperationException("Cannot copy files.");
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) {
		throw new UnsupportedOperationException("Cannot move files.");
	}

	@Override
	public boolean isSameFile(Path a, Path b) {
		if (b == null || a.getFileSystem() != b.getFileSystem()) {
			return false;
		}

		return a.normalize().toAbsolutePath().equals(b.normalize().toAbsolutePath());
	}

	@Override
	public boolean isHidden(Path path) {
		return false;
	}

	@Override
	public FileStore getFileStore(Path path) {
		return null;
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		if (type == null)
			throw new NullPointerException();
		if (type == BasicFileAttributeView.class)
			return (V) new NeoForgeFileAttributeView((NeoForgePath) path);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		// unconditionally support BasicFileAttributes and ZipFileAttributes
		if (type == BasicFileAttributes.class) {
			return (A) this.readBasicAttributes((NeoForgePath) path);
		}

		throw new UnsupportedOperationException("Attributes of type " + type.getName() + " not supported");
	}

	BasicFileAttributes readBasicAttributes(NeoForgePath path) throws IOException {
		var entry = path.getFileSystem().resolveEntry(path);

		if (entry == null) {
			throw new NoSuchFileException(path.toString());
		}

		return new NeoForgeFileAttributes(
				entry,
				entry instanceof NeoForgeFileSystem.File file ? file.resource().attributes() : null
		);
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
			throws IOException {
		String view;
		String attrs;
		int colonPos = attributes.indexOf(':');
		if (colonPos == -1) {
			view = "basic";
			attrs = attributes;
		} else {
			view = attributes.substring(0, colonPos++);
			attrs = attributes.substring(colonPos);
		}

		if (!view.equals("basic")) {
			throw new UnsupportedOperationException("View <" + view + "> is not supported.");
		}

		var parsedAttrs = this.parseAttributes(attrs);
		var readAttributes = this.readBasicAttributes((NeoForgePath) path);

		var map = new LinkedHashMap<String, Object>();
		this.appendAttributes(map, parsedAttrs, "size", readAttributes::size);
		this.appendAttributes(map, parsedAttrs, "creationTime", readAttributes::creationTime);
		this.appendAttributes(map, parsedAttrs, "lastAccessTime", readAttributes::lastAccessTime);
		this.appendAttributes(map, parsedAttrs, "lastModifiedTime", readAttributes::lastModifiedTime);
		this.appendAttributes(map, parsedAttrs, "isDirectory", readAttributes::isDirectory);
		this.appendAttributes(map, parsedAttrs, "isRegularFile", readAttributes::isRegularFile);
		this.appendAttributes(map, parsedAttrs, "isSymbolicLink", readAttributes::isSymbolicLink);
		this.appendAttributes(map, parsedAttrs, "isOther", readAttributes::isOther);
		this.appendAttributes(map, parsedAttrs, "fileKey", readAttributes::fileKey);
		return map;
	}

	private Predicate<String> parseAttributes(String attrs) {
		if (attrs.equals("*")) {
			return YumiPredicates.alwaysTrue();
		} else {
			var set = Set.of(attrs.split(","));
			return set::contains;
		}
	}

	private void appendAttributes(
			Map<String, Object> map, Predicate<String> attrs, String attr, Supplier<Object> data
	) {
		if (attrs.test(attr)) {
			map.put(attr, data.get());
		}
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
		this.checkWritable();
	}

	private void checkWritable() {
		throw new ReadOnlyFileSystemException();
	}
}
