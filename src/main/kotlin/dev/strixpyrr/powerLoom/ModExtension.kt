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

import dev.strixpyrr.powerLoom.environment.ModEnvironment
import dev.strixpyrr.powerLoom.internal.toModId
import dev.strixpyrr.powerLoom.metadata.MutableFabricMod
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.Path

class ModExtension internal constructor(private val project: Project)
{
	var metadata = MutableFabricMod()
	
	/**
	 * Embeds the specified license file into the jar. If the path is relative, it
	 * will be interpreted from the root project directory.
	 */
	fun embedLicense(path: Path = Path("LICENSE")) = project.embedLicense(path)
	
	internal fun populateMetadataFrom(project: Project) = metadata()
	{
		id      = project.name.toModId()
		version = "${project.version}"
		
		metadata.run()
		{
			name = project.name
			
			project.description?.let { description = it }
		}
	}
	
	val environment = ModEnvironment(project)
	
	companion object
	{
		internal const val Name = "mod"
	}
}