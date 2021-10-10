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

import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genBareMinimumScript
import dev.strixpyrr.powerLoomTest.MetadataGenerationTest.genBlankScript
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okio.BufferedSink
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import kotlin.io.path.Path

object MetadataGenerationTest : StringSpec(
{
	"Generates bare-minimum fabric.mod.json"()
	{
		val gradle =
			Gradle.createGradle(
				projectPath = Path("metadata/bareMin"),
				arguments   = listOf("generateModMetadata")
			)
		
		gradle.intoBuild    { genBareMinimumScript() }
		gradle.intoSettings {  }
		
		gradle.run().task(":generateModMetadata")?.outcome shouldBe SUCCESS
	}
	
	"Generates bare-minimum fabric.mod.json using replacement file"()
	{
		val gradle =
			Gradle.createGradle(
				projectPath = Path("metadata/bareMinReplFile"),
				arguments   = listOf("generateModMetadata")
			)
		
		gradle.intoBuild    { genBlankScript() }
		gradle.intoSettings {  }
		
		gradle.intoResourceAt("fabric.mod.json")
		{
			writeUtf8(
				// language=json
				"""{"id":"$TestModId","version":"$TestModVersion"}"""
			)
		}
		
		gradle.run().task(":generateModMetadata")?.outcome shouldBe SUCCESS
	}
	
	"Hooks into processResources"()
	{
		val gradle =
			Gradle.createGradle(
				projectPath = Path("metadata/procResHook"),
				arguments   = listOf("processResources")
			)
		
		gradle.intoBuild    { genBareMinimumScript() }
		gradle.intoSettings {  }
		
		gradle.run().task(":generateModMetadata")?.outcome shouldBe SUCCESS
	}
})
{
	@JvmStatic
	fun BufferedSink.genBareMinimumScript()
	{
		// Huh, apparently trimIndent interprets at compile-time for constants. It
		// has apparently been this way since 1.3.40, but I never realized it. Pog
		writeUtf8("$ScriptPrefix$ScriptRequiredFields$ScriptSuffix".trimIndent())
	}
	
	@JvmStatic
	fun BufferedSink.genBlankScript()
	{
		writeUtf8("$ScriptPrefix$ScriptSuffix".trimIndent())
	}
	
	// language=kts
	private const val ScriptPrefix =
		"""
		plugins {
			kotlin("jvm") version "1.5.31"
			id("dev.strixpyrr.power-loom")
		}
		
		mod {
			metadata {
		"""
	
	// language=kts
	private const val ScriptSuffix =
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
}