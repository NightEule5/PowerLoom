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
package dev.strixpyrr.powerLoom.metadata

import dev.strixpyrr.powerLoom.internal.toModDepVersion
import dev.strixpyrr.powerLoom.internal.toModId
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency

internal fun DependencyContainer.populateFrom(configuration: Configuration)
{
	for (dependency in configuration.dependencies)
		dependency.toModDependency(container = this)
}

private fun Dependency.toModDependency(container: DependencyContainer)
{
	val modId = name.toModId()
	
	when (this)
	{
		is ExternalDependency -> versionConstraint.run()
		{
			val strict    =    strictVersion.ifEmpty { null }
			val required  =  requiredVersion.ifEmpty { null }
			val preferred = preferredVersion.ifEmpty { null }
			val rejected  = rejectedVersions
			
			val versions = mutableList<String>(2)
			
			if (preferred != null)
				versions += preferred.toModDepVersion()
			
			versions +=
				(strict ?: required).toModDepVersion(
					canBeUpgraded = strict == null
				)
			
			container.depends[modId] = versions
			
			if (rejected.isNotEmpty())
			{
				val breaking = container.breaks[modId].let()
				{ existing ->
					mutableList(
						existing sizeOr 0 + rejected.size,
						existing.orEmpty()
					)
				}
				
				container.breaks[modId] =
					rejected.mapTo(breaking) { it.toModDepVersion() }
			}
		}
		else                  ->
			container.depends[modId] =
				version.toModDepVersion(
					canBeUpgraded = true
				)
	}
}

private operator fun MutableMap<String, List<String>>.set(key: String, value: String)
{
	this[key] = this[key].orEmpty() + value
}