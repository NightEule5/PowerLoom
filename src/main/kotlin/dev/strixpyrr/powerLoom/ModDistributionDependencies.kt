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
package dev.strixpyrr.powerLoom

import dev.strixpyrr.powerLoom.internal.toModId
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.MutableVersionConstraint
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.reflect.TypeOf.typeOf

// https://git.io/JiCD7
// https://git.io/JiCbr

internal const val ModrinthMavenGroup = "maven.modrinth"
internal const val    CurseMavenGroup = "curse.maven"

internal fun Project.applyModDistExtensions()
{
	dependencies.extensions.let()
	{
		  ModrinthDependencyExtension(target = it)
		CurseMavenDependencyExtension(target = it)
	}
}

// Extensions

sealed interface ModDistDependencyExtension
{
	// Todo: Support full version constraint syntax.
	
	operator fun invoke(id: String, version: String): ModDependency
	
	sealed interface Modrinth : ModDistDependencyExtension
	{
		override fun invoke(id: String, version: String): ModrinthModDependency
	}
	
	sealed interface CurseMaven : ModDistDependencyExtension
	{
		override fun invoke(id: String, version: String): CurseMavenModDependency
	}
}

internal inline fun <reified E : ModDistDependencyExtension> E.addTo(
	target: ExtensionContainer,
	name: String
) = addTo(target, name, type = E::class.java)

internal fun <E : ModDistDependencyExtension> E.addTo(
	target: ExtensionContainer,
	name: String,
	type: Class<E>
) = target.add(typeOf(type), name, this)

private class ModrinthDependencyExtension(
	target: ExtensionContainer
) : ModDistDependencyExtension.Modrinth
{
	init
	{
		addTo<ModDistDependencyExtension.Modrinth>(target, name = "modrinth")
	}
	
	override fun invoke(id: String, version: String) = ModrinthModDependency(id, version)
}

private class CurseMavenDependencyExtension(
	target: ExtensionContainer
) : ModDistDependencyExtension.CurseMaven
{
	init
	{
		addTo<ModDistDependencyExtension.CurseMaven>(target, name = "curse")
	}
	
	override fun invoke(id: String, version: String) = CurseMavenModDependency(id, version)
}

// Dependency types

sealed class ModDependency : DefaultExternalModuleDependency
{
	constructor(
		group  : String,
		id     : String,
		version: String
	) : super(group, id, version)
	
	constructor(
		id: ModuleIdentifier,
		versionConstraint: MutableVersionConstraint
	) : super(id, versionConstraint)
	
	abstract fun toModId(): String
}

class ModrinthModDependency internal constructor(
	id: String, version: String
) : ModDependency(group = ModrinthMavenGroup, id, version)
{
	override fun toModId() = name.let()
	{
		if (it matches IdRegex)
			TODO("Project Ids are not supported yet.")
		else it.toModId()
	}
	
	companion object
	{
		private val IdRegex = Regex("^[A-Za-z0-9]{8}$")
	}
}

class CurseMavenModDependency internal constructor(
	id: String, version: String
) : ModDependency(group = CurseMavenGroup, id, version)
{
	override fun toModId() = name.substringBeforeLast('-') // Trim the -000000 suffix
}
