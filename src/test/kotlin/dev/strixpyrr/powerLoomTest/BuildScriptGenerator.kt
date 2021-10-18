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
package dev.strixpyrr.powerLoomTest

import okio.BufferedSink

internal object BuildScriptGenerator
{
	@JvmStatic
	fun BufferedSink.genBareMinimum()
	{
		genBlank()
		
		// Huh, apparently trimIndent interprets at compile-time for constants. It
		// has apparently been this way since 1.3.40, but I never realized it. Pog
		writeUtf8(BaseModBlock.trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genDefaultPulling()
	{
		writeUtf8(PluginBlockWithFabric .trimIndent() + "\n\n")
		writeUtf8(ProjectFields         .trimIndent() + "\n\n")
		writeUtf8(FabricDependencyBlocks.trimIndent() + "\n\n")
		writeUtf8(DependencyModBlock    .trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genBlank()
	{
		writeUtf8(BasePluginBlock.trimIndent() + "\n\n")
	}
	
	@JvmStatic
	fun BufferedSink.genProperties()
	{
		writeUtf8(DefaultGradleProperties)
	}
	
	// Plugin declarations
	
	private const val KotlinPluginDeclaration =
		"""kotlin("jvm") version "1.6.0-RC""""
	
	private const val FabricLoomPluginDeclaration =
		"""id("fabric-loom")"""
	
	private const val PowerLoomPluginDeclaration =
		"""id("dev.strixpyrr.powerloom")"""
	
	// Fields
	
	private const val ModBlockRequiredFields =
		"""
				id = "$TestModId"
				version = "$TestModVersion"
		"""
	
	private const val ModBlockDependencyField =
		"""
				dependencies.run {
					val modImplementation by configurations
		
					populate(modImplementation)
				}
		"""
	
	// language=kts
	private const val ProjectFields =
		"""
		version = "$TestModVersion"
		description = "$TestDesc"
		"""
	
	// Blocks
	
	// language=kts
	private const val BasePluginBlock =
		"""
		plugins {
			$KotlinPluginDeclaration
			$PowerLoomPluginDeclaration
		}
		"""
	
	// language=kts
	private const val PluginBlockWithFabric =
		"""
		plugins {
			$KotlinPluginDeclaration
			$FabricLoomPluginDeclaration
			$PowerLoomPluginDeclaration
		}
		"""
	
	// language=kts
	private const val BaseModBlock =
		"""
		mod {
			metadata {
		$ModBlockRequiredFields
			}
		}
		"""
	
	// language=kts
	private const val DependencyModBlock =
		"""
		mod {
			metadata {
		$ModBlockDependencyField
			}
		}
		"""
	
	// language=kts
	private const val FabricDependencyBlocks =
		"""
		repositories {
			maven(url = "https://maven.fabricmc.net")
		}
		
		dependencies {
			minecraft(group = "org.mojang", name = "minecraft", version = "1.17.1")

			mappings(group = "net.fabricmc", name = "yarn", version = "1.17.1+build.61")

			modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = "1.6.5+kotlin.1.5.31")
		}
		"""
	
	// Properties
	
	// language=properties
	private const val DefaultGradleProperties =
		"powerLoom.pullMetadataFromProject=false"
}