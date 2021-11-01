// Copyright 2021 Strixpyrr
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
@file:OptIn(ExperimentalSerializationApi::class)

package dev.strixpyrr.powerLoom.environment

import com.charleskorn.kaml.Yaml
import dev.strixpyrr.powerLoom.internal.enumSetOfAll
import dev.strixpyrr.powerLoom.internal.toCamelCase
import dev.strixpyrr.powerLoom.metadata.FabricMod.Companion.IdRegex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.serializer
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.name

class SupplementalMod internal constructor(val id: String)
{
	init
	{
		require(id matches IdRegex)
	}
	
	var optional = true
	
	var presenceVar = id.toCamelCase()
	
	val includedSources: MutableSet<ModSourceInclusion> = enumSetOfAll()
	
	val configs = mutableListOf<ModConfig>()
	
	inline fun configs(block: ConfigScope.() -> Unit) =
		ConfigScope().block()
	
	inner class ConfigScope
	private constructor(
		private val root: Path
	)
	{
		@PublishedApi
		internal constructor() : this(Path("."))
		
		init
		{
			root.requireRelative()
		}
		
		inline fun at(
			path: Path,
			block: ConfigScope.() -> Unit
		) = child(path).block()
		
		inline fun <reified T : Any> fromResource(resource: Path) =
			add(resource, target = T::class.java)
		
		fun json(path: Path, @Language("json") contents: String)
		{
			path.requireExtension("json")
			
			addString(path, contents)
		}
		
		fun properties(path: Path, @Language("properties") contents: String)
		{
			path.requireExtension("properties")
			
			addString(path, contents)
		}
		
		fun yaml(path: Path, @Language("yaml") contents: String)
		{
			path.requireExtensionIn("yml", "yaml")
			
			addString(path, contents)
		}
		
		fun toml(path: Path, @Language("toml") contents: String)
		{
			path.requireExtension("toml")
			
			addString(path, contents)
		}
		
		inline fun <reified V : Any> config(
			path: Path,
			config: V,
			json: Json = Json.Default
		) = add(path, serializer(), config, json)
		
		inline fun <reified V : Any> config(
			path: Path,
			config: V,
			properties: Properties = Properties.Default
		) = add(path, serializer(), config, properties)
		
		inline fun <reified V : Any> config(
			path: Path,
			config: V,
			yaml: Yaml = Yaml.default
		) = add(path, serializer(), config, yaml)
		
		fun <V : ITomlConfig> config(
			path: Path,
			config: V,
		) = add(path, config)
		
		@PublishedApi
		internal fun child(path: Path) = ConfigScope(root / path)
		
		@PublishedApi
		internal fun add(
			resource: Path,
			target: Class<*>
		)
		{
			configs += ResourceModConfig(root / resource.name, resource, target)
		}
		
		private fun addString(
			path: Path,
			contents: String
		)
		{
			path.requireRelative()
			
			configs += StringModConfig(root / path, contents)
		}
		
		@PublishedApi
		internal fun <V : Any> add(
			path: Path,
			serializer: KSerializer<V>,
			value: V,
			json: Json
		)
		{
			path.requireRelative()
			path.requireExtension("json")
			
			configs += SerializingModConfig.json(root / path, serializer, value, json)
		}
		
		@PublishedApi
		internal fun <V : Any> add(
			path: Path,
			serializer: KSerializer<V>,
			value: V,
			properties: Properties
		)
		{
			path.requireRelative()
			path.requireExtension("properties")
			
			configs += SerializingModConfig.properties(root / path, serializer, value, properties)
		}
		
		@PublishedApi
		internal fun <V : Any> add(
			path: Path,
			serializer: KSerializer<V>,
			value: V,
			yaml: Yaml
		)
		{
			path.requireRelative()
			path.requireExtensionIn("yml", "yaml")
			
			configs += SerializingModConfig.yaml(root / path, serializer, value, yaml)
		}
		
		@PublishedApi
		internal fun <V : ITomlConfig> add(
			path: Path,
			value: V
		)
		{
			path.requireRelative()
			path.requireExtensionIn("toml")
			
			configs += SerializingModConfig.toml(root / path, value)
		}
	}
}

internal fun Path.requireRelative() = require(!isAbsolute)
{
	"Config paths must be relative."
}

internal fun Path.requireExtension(expected: String) = extension.let()
{
	require(it == expected)
	{
		contentsMustMatchExtension(".$expected", it)
	}
}

internal fun Path.requireExtensionIn(vararg expected: String) = extension.let()
{
	require(it in expected)
	{
		val expectedList = expected.joinToString(
			limit = expected.size - 1,
			truncated = ", or .${expected.last()}"
		) { s -> ".$s" }
		
		contentsMustMatchExtension(expectedList, it)
	}
}

private fun contentsMustMatchExtension(expected: String, actual: String) =
	"Config contents must match their format's extension: $expected was expected" +
	", but the path extension was .$actual."