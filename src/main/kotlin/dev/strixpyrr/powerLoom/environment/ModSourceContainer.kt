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

import dev.strixpyrr.powerLoom.environment.ModSource.*
import dev.strixpyrr.powerLoom.internal.addedTo
import okhttp3.HttpUrl
import java.net.URL

class ModSourceContainer internal constructor()
{
	private val set = linkedSetOf<ModSource>()
	
	inline operator fun invoke(populate: ModSourceContainer.() -> Unit) = populate()
	
	fun modrinth(): ModSource = Modrinth addedTo set
	fun curse   (): ModSource = Curse    addedTo set
	fun maven   (): ModSource = Maven    addedTo set
	
	fun url(url: String,  name: String? = null): ModSource = Url(name, url) addedTo set
	fun url(url: URL,     name: String? = null): ModSource = Url(name, url) addedTo set
	fun url(url: HttpUrl, name: String? = null): ModSource = Url(name, url) addedTo set
}