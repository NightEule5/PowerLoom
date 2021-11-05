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
package dev.strixpyrr.powerLoom.environment

import dev.strixpyrr.powerLoom.internal.Property.EnvironmentMods
import dev.strixpyrr.powerLoom.internal.PropertyContainer
import dev.strixpyrr.powerLoom.internal.addedTo
import dev.strixpyrr.powerLoom.internal.camelCaseToPascal
import org.gradle.api.plugins.ExtraPropertiesExtension

class SupplementalModContainer
{
	private val mods = mutableListOf<SupplementalMod>()
	
	inline operator fun invoke(populate: SupplementalModContainer.() -> Unit) = populate()
	
	inline operator fun String.invoke(
		block: SupplementalMod.() -> Unit
	) = get(id = this).block()
	
	operator fun get(id: String) =
		mods.find { it.id == id } ?:
		(SupplementalMod(id) addedTo mods)
	
	internal fun getDownloadedMods(
		properties: PropertyContainer,
		extra: ExtraPropertiesExtension
	) = mods.filter()
	{
		if (it.optional)
		{
			val presenceVar = it.presenceVar
			
			when
			{
				!properties[EnvironmentMods, presenceVar]                    -> false
				extra["download${presenceVar.camelCaseToPascal()}"] == false -> false
				else                                                         ->
				{
					val envVarKey = "download_$presenceVar"
					
					val env = System.getenv()
					
					val entry = env.entries.find()
					{ (key, _) ->
						key.equals(envVarKey, ignoreCase = true)
					}
					
					entry == null || entry.value != "false"
				}
			}
		}
		else true
	}
}