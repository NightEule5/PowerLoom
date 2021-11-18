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
package dev.strixpyrr.powerLoom.internal

import dev.strixpyrr.powerLoom.internal.Property.*
import org.gradle.api.Project

private const val PropertyDomain = "powerLoom"

private const val      CreateDefaultTasksProperty = "$PropertyDomain.createDefaultTasks"
private const val PullMetadataFromProjectProperty = "$PropertyDomain.pullMetadataFromProject"
private const val         EnvironmentModsProperty = "$PropertyDomain.environment.mods"

internal enum class Property { CreateDefaultTasks, PullMetadataFromProject, EnvironmentMods }

@Suppress("NOTHING_TO_INLINE")
internal inline fun Project.properties() = PropertyContainer(properties)

@JvmInline
internal value class PropertyContainer(private val map: Map<String, Any?>)
{
	@Suppress("UNCHECKED_CAST")
	operator fun get(property: Property, key: String = "") = when (property)
	{
		CreateDefaultTasks      -> get(     CreateDefaultTasksProperty, default = true)
		PullMetadataFromProject -> get(PullMetadataFromProjectProperty, default = true)
		EnvironmentMods         -> get("$EnvironmentModsProperty.$key", default = true)
	}
	
	@Suppress("SameParameterValue")
	private fun get(property: String, default: Boolean) =
		try
		{
			map[property].parseBool(default)
		}
		catch (e: IllegalArgumentException)
		{
			throw Exception(
				"The $property property was found but its value was invalid.", e
			)
		}
}

private fun Any?.parseBool(default: Boolean) = when (this)
{
	null      -> default
	is String -> toBooleanStrict()
	else      ->
		throw IllegalArgumentException(
			"A boolean parsable string was expected but a value of type ${
				this::class.simpleName
			} was received."
		)
}