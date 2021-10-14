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

import dev.strixpyrr.powerLoom.internal.Property.CreateDefaultTasks
import dev.strixpyrr.powerLoom.internal.Property.PullMetadataFromProject
import dev.strixpyrr.powerLoom.internal.get
import dev.strixpyrr.powerLoom.internal.properties
import dev.strixpyrr.powerLoom.tasks.GenerateModMetadataTask
import dev.strixpyrr.powerLoom.tasks.toGenerateModMetadataName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class PowerLoomPlugin : Plugin<Project>
{
	override fun apply(target: Project)
	{
		target.plugins.run()
		{
			if (!hasPlugin("org.jetbrains.kotlin.jvm"          ) &&
			    !hasPlugin("org.jetbrains.kotlin.multiplatform"))
				throw Exception("The Kotlin plugin is not applied.")
		}
		
		val props = target.properties()
		
		val mod = ModExtension()
		
		target.extensions.add(ModExtension::class.java, ModExtension.Name, mod)
		
		if (props[CreateDefaultTasks])
		{
			val kotlin: KotlinProjectExtension = target.extensions["kotlin"]
			
			val sourceSets = kotlin.sourceSets
			
			target.tasks.run()
			{
				sourceSets.forEach()
				{
					if (it.name.contains("main", ignoreCase = true))
					{
						// Register would be ideal, but the task has to be created
						// to set a dependency in resources. The alternative would
						// be to find the processResources task for the current
						// source set, but KotlinSourceSet doesn't provide that
						// information.
						create<GenerateModMetadataTask>(it.toGenerateModMetadataName())
							.fromSourceSet(it)
					}
				}
			}
		}
		
		if (props[PullMetadataFromProject])
			target.afterEvaluate(mod::populateFrom)
	}
}