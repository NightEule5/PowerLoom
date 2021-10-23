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

import org.openapitools.client.infrastructure.ApiClient

internal object Modrinth
{
	@JvmStatic
	fun createClient(staging: Boolean = false) = Client(staging)
	
	@JvmStatic
	fun createApiClient(staging: Boolean) =
		ApiClient(
			baseUrl = "https://${
				if (staging) "staging-" else ""
			}api.modrinth.com/v2/"
		)
	
	private inline fun <reified S> ApiClient.createService() =
		createService(S::class.java)
	
	@JvmInline
	value class Client private constructor(
		private val client: ApiClient
	)
	{
		constructor(staging: Boolean) : this(createApiClient(staging))
		
		val auth     get() = ModrinthAuth    .Service(client.createService())
		val projects get() = ModrinthProjects.Service(client.createService())
		val users    get() = ModrinthUsers   .Service(client.createService())
		val versions get() = ModrinthVersions.Service(client.createService())
	}
}