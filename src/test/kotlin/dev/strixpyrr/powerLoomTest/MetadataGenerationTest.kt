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

import dev.strixpyrr.powerLoom.metadata.FabricMod
import dev.strixpyrr.powerLoomTest.BuildScriptGenerator.genBareMinimum
import dev.strixpyrr.powerLoomTest.BuildScriptGenerator.genBlank
import dev.strixpyrr.powerLoomTest.BuildScriptGenerator.genDefaultPulling
import dev.strixpyrr.powerLoomTest.BuildScriptGenerator.genProperties
import dev.strixpyrr.powerLoomTest.Gradle.outcomeOf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
		
		gradle.intoBuild      { genBareMinimum() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
		
		metadataOutput(project) shouldBe FabricMod(TestModId, TestModVersion)
	}
	
	"Generates bare-minimum fabric.mod.json using replacement file"()
	{
		val project = Path("metadata/bareMinReplFile")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = GenerateModMetadataName
			)
		
		gradle.intoBuild      { genBlank() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.intoResourceAt(FabricModJson)
		{
			writeUtf8(BaseJson)
		}
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
		
		metadataOutput(project) shouldBe FabricMod(TestModId, TestModVersion)
	}
	
	"Hooks into processResources"()
	{
		val project = Path("metadata/procResHook")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = ProcessResourcesName
			)
		
		gradle.intoBuild      { genBareMinimum() }
		gradle.intoSettings   {  }
		gradle.intoProperties { genProperties() }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
		
		metadataOutput(project) shouldBe FabricMod(TestModId, TestModVersion)
	}
	
	"Pulls defaults from project"()
	{
		val project = Path("metadata/projectDefaults")
		
		val gradle =
			Gradle.createGradle(
				projectPath = project,
				targetTask  = GenerateModMetadataName
			)
		
		gradle.intoBuild    { genDefaultPulling() }
		gradle.intoSettings {  }
		
		deleteMetadataOutput(project)
		
		gradle.run() outcomeOf GenerateModMetadataPath shouldBe SUCCESS
		
		metadataOutput(project) shouldBe FabricMod(
			id          = "project-defaults",
			version     = TestModVersion,
			description = TestDesc,
			name        = "projectDefaults",
			depends     = mapOf(
				"fabric-language-kotlin" to
					listOf(">=1.6.5+kotlin.1.5.31")
			)
		)
	}
})