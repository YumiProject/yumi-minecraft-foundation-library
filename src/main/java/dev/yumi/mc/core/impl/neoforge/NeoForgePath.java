/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *   Copyright 2022, 2023 The Quilt Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.yumi.mc.core.impl.neoforge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoForgePath implements Path {
	static final String ROOT = "/";
	private static final String SELF = ".";
	private static final String PARENT = "..";

	private final NeoForgeFileSystem fileSystem;
	private final @Nullable NeoForgePath parent;
	private final String name;
	private final boolean absolute;
	private final boolean normalized;
	private final int nameCount;
	private final int hash;

	public NeoForgePath(
			NeoForgeFileSystem fileSystem,
			@Nullable NeoForgePath parent,
			String name
	) {
		this.fileSystem = fileSystem;
		this.parent = parent;
		this.name = name;

		if (ROOT.equals(name)) {
			if (parent != null) {
				throw new IllegalArgumentException("Root paths cannot have a parent!");
			}
			this.nameCount = 1;
			this.absolute = true;
			this.normalized = true;
		} else {
			int count = 0;
			if (parent != null) count += parent.getNameCount();
			if (!name.isEmpty()) count++;

			this.nameCount = count;

			boolean isNormalName = !name.equals(PARENT) && !name.equals(SELF);

			if (parent == null) {
				this.absolute = false;
				this.normalized = isNormalName;
			} else {
				this.absolute = parent.isAbsolute();
				this.normalized = parent.normalized && isNormalName;
			}
		}

		this.hash = fileSystem.hashCode() * 31
				+ (parent == null ? name.hashCode() : (parent.hash * 31 + name.hashCode()));
	}

	@Override
	public @NotNull NeoForgeFileSystem getFileSystem() {
		return this.fileSystem;
	}

	@Override
	public boolean isAbsolute() {
		return this.absolute;
	}

	public boolean isRoot() {
		return this.name.equals(ROOT);
	}

	@Override
	public Path getRoot() {
		if (this.absolute) {
			return this.fileSystem.root;
		}

		return null;
	}

	@Override
	public Path getFileName() {
		if (this.name.isEmpty()) {
			return null;
		} else if (parent == null) {
			return this;
		} else {
			return this.fileSystem.createPath(null, this.name);
		}
	}

	@Override
	public @Nullable NeoForgePath getParent() {
		return this.parent;
	}

	@Override
	public int getNameCount() {
		return this.nameCount;
	}

	@Override
	public @NotNull Path getName(int index) {
		if (index < 0 || index >= this.getNameCount()) {
			throw new IllegalArgumentException("Index out of bounds.");
		}

		if (index == 0 && this.parent == null) return this;
		if (index == this.getNameCount() - 1) return this.fileSystem.createPath(null, this.name);

		var p = this;
		for (int i = index + 1; i < this.getNameCount(); i++) {
			if (p == null)
				throw new IllegalStateException("Name count is incorrect: cannot find name at index " + index);

			p = p.parent;
		}

		if (p == null)
			throw new IllegalStateException("Name count is incorrect: cannot find name at index " + index);

		return this.fileSystem.createPath(null, p.name);
	}

	@Override
	public @NotNull Path subpath(int beginIndex, int endIndex) {
		if (beginIndex < 0) {
			throw new IllegalArgumentException("beginIndex < 0!");
		}

		if (endIndex > this.getNameCount()) {
			throw new IllegalArgumentException("endIndex > getNameCount()!");
		}

		var end = this;

		for (int i = this.getNameCount(); i > endIndex; i--) {
			if (end == null)
				throw new IllegalStateException("Name count is incorrect: cannot find name at index " + endIndex);

			end = end.parent;
		}

		var from = end;
		var names = new ArrayList<String>();

		for (int i = endIndex - 1; i > beginIndex; i--) {
			assert from != null;
			from = from.parent;
			assert from != null;
			names.add(from.name);
		}

		if (from == null)
			throw new IllegalStateException("Name count is incorrect: cannot find name at index " + endIndex);

		String fromS = from.name;
		if (fromS.startsWith("/")) {
			fromS = fromS.substring(1);
		}

		var path = this.fileSystem.createPath(null, fromS);

		for (var sub : names.reversed()) {
			path = path.resolve(sub);
		}

		return path;
	}

	@Override
	public boolean startsWith(@NotNull Path other) {
		if (other instanceof NeoForgePath o) {
			if (this.isAbsolute() != o.isAbsolute()) {
				return false;
			}
			if (o.getNameCount() > this.getNameCount()) {
				return false;
			}

			var p = this;

			do {
				if (other.equals(p)) {
					return true;
				}
			} while ((p = p.parent) != null);
		}

		return false;
	}

	@Override
	public boolean startsWith(@NotNull String other) {
		return this.startsWith(this.fileSystem.getPath(other));
	}

	@Override
	public boolean endsWith(@NotNull Path other) {
		if (other instanceof NeoForgePath o) {
			var t = this;

			while (o != null && t != null) {
				if (!t.name.equals(o.name)) {
					return false;
				}

				o = o.parent;
				t = t.parent;
			}

			return o == null;
		} else {
			return false;
		}
	}

	@Override
	public boolean endsWith(@NotNull String other) {
		return this.endsWith(this.fileSystem.getPath(other));
	}

	@Override
	public @NotNull NeoForgePath normalize() {
		if (this.normalized) {
			return this;
		}

		if (SELF.equals(this.name)) {
			if (this.parent != null) {
				return this.parent.normalize();
			}
			return this;
		}

		if (PARENT.equals(this.name)) {
			var path = this;
			if (this.parent == null) {
				return path;
			}
			var p = this.parent.normalize();
			if (PARENT.equals(p.name)) {
				// ../..
				// Since the parent is also ".." then it must be .. all the way to the root
				// so we can't normalise any further
				return p.resolve(this.name);
			} else if (p.parent != null) {
				return p.parent;
			} else {
				return this.fileSystem.createPath(null, SELF);
			}
		}

		if (parent == null) {
			return this;
		}

		var p = this.parent.normalize();
		if (p == parent) {
			return this;
		} else {
			return p.resolve(this.name);
		}
	}

	@Override
	public @NotNull NeoForgePath resolve(@NotNull Path other) {
		if (other.isAbsolute()) {
			return (NeoForgePath) other;
		}

		if (other.getNameCount() == 0) {
			return this;
		}
		var o = (NeoForgePath) other;

		var stack = new ArrayDeque<NeoForgePath>();

		do {
			stack.push(o);
		} while ((o = o.parent) != null);

		NeoForgePath p = this;

		while (!stack.isEmpty()) {
			p = this.fileSystem.createPath(p, stack.pop().name);
		}

		return p;
	}

	@Override
	public @NotNull NeoForgePath resolve(String other) {
		var p = this;
		for (String s : other.split(this.fileSystem.getSeparator())) {
			if (!s.isEmpty()) {
				p = this.fileSystem.createPath(p, s);
			}
		}
		return p;
	}

	@Override
	public @NotNull Path relativize(@NotNull Path other) {
		var o = (NeoForgePath) other;
		if (o.equals(this)) {
			return this.fileSystem.createPath(null, "");
		}

		if (this.isAbsolute() != o.isAbsolute()) {
			throw new IllegalArgumentException(
					"You can only relativize paths if they are both absolute, OR both relative - not one and the other!"
			);
		}

		List<String> names = this.normalize().names();
		List<String> oNames = o.normalize().names();

		int i;
		for (i = 0; i < Math.min(names.size(), oNames.size()); i++) {
			String a = names.get(i);
			String b = oNames.get(i);
			if (!a.equals(b)) {
				break;
			}
		}

		NeoForgePath path = null;
		for (int j = i; j < names.size(); j++) {
			if (path == null) {
				path = this.fileSystem.createPath(null, PARENT);
			} else {
				path = path.resolve(PARENT);
			}
		}

		for (int j = i; j < oNames.size(); j++) {
			if (path == null) {
				path = this.fileSystem.createPath(null, oNames.get(j));
			} else {
				path = path.resolve(oNames.get(j));
			}
		}

		return path;
	}

	@Override
	public @NotNull URI toUri() {
		if (!this.isAbsolute()) return this.toAbsolutePath().toUri();
		try {
			// Constructing as components ensures proper quoting on most
			// Adding the port stores the important info in both the authority and host
			return new URI(
					this.fileSystem.provider().getScheme(),
					this.fileSystem.name + '!' + this,
					null
			).normalize();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public @NotNull NeoForgePath toAbsolutePath() {
		if (this.isAbsolute()) {
			return this;
		}

		return this.fileSystem.root.resolve(this);
	}

	@Override
	public @NotNull NeoForgePath toRealPath(@NotNull LinkOption... options) {
		return this.toAbsolutePath();
	}

	@Override
	public @NotNull WatchKey register(
			@NotNull WatchService watcher,
			WatchEvent.Kind<?> @NotNull [] events,
			@NotNull WatchEvent.Modifier @NotNull ... modifiers
	) throws IOException {
		throw new UnsupportedOperationException();
	}

	List<String> names() {
		if (parent == null) {
			if (this.isRoot()) {
				return List.of();
			} else {
				return List.of(this.name);
			}
		}
		var list = new ArrayList<String>(this.getNameCount());
		NeoForgePath p = this;
		do {
			if (p.isRoot()) {
				break;
			}
			list.add(p.name);
		} while ((p = p.getParent()) != null);
		return list.reversed();
	}

	@Override
	public int compareTo(@NotNull Path other) {
		return this.toString().compareTo(other.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		var o = (NeoForgePath) obj;
		return this.fileSystem == o.fileSystem
				&& this.absolute == o.absolute
				&& this.normalized == o.normalized
				&& this.nameCount == o.nameCount
				&& this.name.equals(o.name)
				&& Objects.equals(this.parent, o.parent);
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	@Override
	public @NotNull String toString() {
		if (this.isRoot()) {
			return ROOT;
		} else if (this.parent != null) {
			return (this.parent.name.equals(ROOT) ? "" : this.parent.toString())
					+ this.fileSystem.getSeparator() + this.name;
		} else {
			return this.name;
		}
	}
}
