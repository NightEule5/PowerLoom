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
package dev.strixpyrr.powerLoom.modDistributionPlatforms

import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetContainer
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.ProjectsService

internal object ModrinthProjects
{
	@JvmStatic
	suspend fun get(service: Service, id: String) =
		service.value.getProject(id)
	
	@JvmStatic
	suspend fun search(service: Service, query: String? = null) =
		service.value.searchProjects(query, null, null, null, null, null, null)
	
	@JvmStatic
	suspend fun search(service: Service, query: String? = null, facets: FacetContainer) =
		service.value.searchProjects(
			query,
			facets = facets.toList(),
			null,
			null,
			null,
			null,
			null
		)
	
	@JvmInline
	value class Service(
		val value: ProjectsService
	)
}