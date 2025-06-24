/*
 * Copyright 2025 Yumi Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * <h1>Entrypoints</h1>
 * <p>
 * Entrypoints are points of entry for your mod which translates to a key in mod manifests that are linked to a Java type.
 *
 * <h2 id="registering_entrypoints">Registering entrypoints</h2>
 * <p>
 * Since the goal of entrypoints is to give points of entry,
 * instead of registering them through Java code like you would for {@linkplain dev.yumi.commons.event.Event an event},
 * you need to register them in your mod manifest.
 * <p>
 * This takes the form of a {@code yumi:entrypoints} object whose keys are entrypoint keys like {@value ModInitializer#ENTRYPOINT_KEY},
 * and whose values take either the form of an entrypoint string, an object with a {@code value} key,
 * or an array of the preceding types.
 * <br />
 * The entrypoint string may take the form of a fully-qualified class name ({@code org.example.mod.ModInitializer} for example),
 * and optionally a second part after a {@code ::} separator either pointing to an instance field (which must be static final),
 * or a method reference.
 * <p>
 * Mod manifests take different forms depending on your mod loader.
 *
 * <h3>Fabric</h3>
 * <h4>Built-in entrypoint system</h4>
 * <p>
 * Fabric has a built-in entrypoint system (which this library inspired from), this means in your {@code fabric.mod.json}
 * you can use the regular Fabric {@code entrypoints} key:
 * <pre>{@code "entrypoints": {
 * 	"yumi:init": [
 * 		"org.example.mod.ModInitializer"
 * 	]
 * }}</pre>
 * <p>
 * This also comes with the advantage of being able to use Fabric's language provider if necessary.
 *
 * <h4>Yumi-provided entrypoint system</h4>
 * <p>
 * Yumi also has its own entrypoint system which can be specified in a {@code fabric.mod.json}'s {@code custom} block:
 * <pre>{@code "custom": {
 * 	"yumi:entrypoints": {
 * 		"yumi:init": [
 * 			"org.example.mod.ModInitializer"
 * 		]
 * 	}
 * }}</pre>
 *
 * <h3>NeoForge</h3>
 * <p>
 * NeoForge does not have a built-in entrypoint system unlike Fabric, as it relies on classpath analysis and annotations instead.
 * The entrypoint system takes a similar form in a {@code neoforge.mods.toml} file, in a {@code modproperties} block:
 * <pre>{@code [modproperties.modid."yumi:entrypoints"]
 * "yumi:init": "org.example.mod.ModInitializer"}</pre>
 *
 * <h2>Calling entrypoints</h2>
 * <p>
 * The advantage of entrypoints is you can define your own entrypoints for other mods to use,
 * this is especially practical for APIs provided by mods and can lead to an organic load order that doesn't have to worry about
 * loading before or after other mods.
 * <p>
 * You can call {@link dev.yumi.mc.core.api.YumiMods#getEntrypoints(java.lang.String, java.lang.Class)} to get a collection of
 * {@linkplain dev.yumi.mc.core.api.entrypoint.EntrypointContainer entrypoint containers},
 * or you can call the shortcut method
 * {@link dev.yumi.mc.core.api.YumiMods#invokeEntrypoints(java.lang.String, java.lang.Class, java.util.function.BiConsumer)}
 * for invocation.
 */
package dev.yumi.mc.core.api.entrypoint;
