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
@file:Suppress("BlockingMethodInNonBlockingContext")

package dev.strixpyrr.powerLoomTest

import dev.strixpyrr.powerLoomTest.Gradle.outcomeOf
import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genBareMinimumScript
import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genBlankScript
import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genDependencyScript
import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.source
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import kotlin.io.path.Path

object MetadataGenerationTest : StringSpec(
{
	"Generates bare-minimum fabric.mod.json"()
	{
		val project = Path("metadata/bareMin")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = GenerateModMetadataName
			)
		
		gradle.intoBuild      { genBareMinimumScript() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
	}
	
	"Generates bare-minimum fabric.mod.json using replacement file"()
	{
		val project = Path("metadata/bareMinReplFile")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = GenerateModMetadataName
			)
		
		gradle.intoBuild      { genBlankScript() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.intoResourceAt(FabricModJson)
		{
			writeUtf8(BaseJson)
		}
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
	}
	
	"Hooks into processResources"()
	{
		val project = Path("metadata/procResHook")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = ProcessResourcesName
			)
		
		gradle.intoBuild      { genBareMinimumScript() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
	}
	
	"Pulls defaults from project"()
	{
		val project = Path("metadata/projectDefaults")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = GenerateModMetadataName
			)
		
		gradle.intoBuild    { genDependencyScript() }
		gradle.intoSettings {  }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
		
		Path("run/metadata/projectDefaults/build/generated-resources/power-loom-main/$FabricModJson")
			.source()
			.buffer()
			.use(BufferedSource::readUtf8) shouldBe
				NameDescDepJson
					.replaceFirst(TestModId, "project-defaults")
					.replaceFirst(TestName, "projectDefaults")
	}
})
{
	@JvmStatic
	fun BufferedSink.genBareMinimumScript()
	{
		// Huh, apparently trimIndent interprets at compile-time for constants. It
		// has apparently been this way since 1.3.40, but I never realized it. Pog
		writeUtf8("$ScriptPrefix$ScriptModBlockPrefix$ScriptRequiredFields$ScriptModBlockSuffix".trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genBlankScript()
	{
		writeUtf8("$ScriptPrefix$ScriptModBlockPrefix$ScriptModBlockSuffix".trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genDependencyScript()
	{
		writeUtf8("$ScriptPrefix$ScriptDependencies$ScriptModBlockPrefix$ScriptDependencyField$ScriptModBlockSuffix".trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genProperties()
	{
		writeUtf8("powerLoom.pullMetadataFromProject=false")
	}
	
	// language=kts
	private const val ScriptPrefix =
		"""
		plugins {
			kotlin("jvm") version "1.6.0-RC"
			id("fabric-loom")
			id("dev.strixpyrr.powerloom")
		}
		
		version = "$TestModVersion"
		description = "$TestDesc"
		"""
	
	// language=kts
	private const val ScriptModBlockPrefix =
		"""
		mod {
			metadata {
		"""
	
	// language=kts
	private const val ScriptModBlockSuffix =
		"""
			}
		}
		"""
	
	// language=kts
	private const val ScriptRequiredFields =
		"""
				id = "$TestModId"
				version = "$TestModVersion"
		"""
	
	// language=kts
	private const val ScriptDependencyField =
		"""
				dependencies.run {
					val modImplementation by configurations

					populate(modImplementation)
				}
		"""
	
	// language=kts
	private const val ScriptDependencies =
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
}