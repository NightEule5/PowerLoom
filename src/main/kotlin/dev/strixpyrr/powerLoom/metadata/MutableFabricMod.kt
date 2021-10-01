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

import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

private const val EntryPointCapacity = 4
private const val  NestedJarCapacity = 8
private const val      MixinCapacity = 4
private const val    AdapterCapacity = 2
private const val DependencyCapacity = 8
private const val     PersonCapacity = 4
private const val    ContactCapacity = 2

class MutableFabricMod(
	var id     : String = Unset,
	var version: String = Unset,
	var environment: Environment        = Environment.Either,
	val entryPoints: MutableEntryPoints = MutableEntryPoints(),
	val jars  : MutableList<NestedJar>  = mutableList(NestedJarCapacity),
	val mixins: MutableList<Mixin>      = mutableList(    MixinCapacity),
	val languageAdapters: MutableMap<String, String> = mutableMap(AdapterCapacity),
	val dependencies    : DependencyContainer        = DependencyContainer(),
	val metadata        : MetadataContainer = MetadataContainer(),
	var accessWidener   : Path? = null
)
{
	constructor(immutable: FabricMod) : this(
		id      = immutable.id,
		version = immutable.version,
		environment = immutable.environment,
		entryPoints = immutable.entryPoints.intoMutable(),
		jars   = immutable.jars  .toMutableListWithSpaceFor(NestedJarCapacity),
		mixins = immutable.mixins.toMutableListWithSpaceFor(    MixinCapacity),
		languageAdapters = immutable.languageAdapters.toMutableMapWithSpaceFor(AdapterCapacity),
		dependencies     = DependencyContainer(immutable),
		metadata         =   MetadataContainer(immutable),
		accessWidener    = immutable.accessWidener.toPath()
	)
	
	fun populateFrom(immutable: FabricMod): Unit = TODO()
	
	inline operator fun invoke(populate: MutableFabricMod.() -> Unit) = populate()
	
	fun freeze(): FabricMod
	{
		val id      = when (val id = id)
		{
			Unset -> throw FieldNotSetException("id")
			else  -> id
		}
		
		val version = when (val version = version)
		{
			Unset -> throw FieldNotSetException("version")
			else  -> version
		}
		
		val entryPoints = entryPoints.freeze()
		
		val (depends, recommends, suggests, conflicts, breaks) = dependencies
		
		val (name, description, authors, contributors, contact, license, icon) = metadata
		
		return FabricMod(
			id,
			version,
			environment,
			entryPoints,
			jars,
			mixins,
			languageAdapters,
			depends,
			recommends,
			suggests,
			conflicts,
			breaks,
			name,
			description,
			authors,
			contributors,
			contact.freeze(),
			license,
			icon,
			accessWidener
		)
	}
	
	companion object
	{
		private const val Unset = ""
		
		internal fun String?.toPath() = if (this == null) null else Path(this)
	}
}

internal fun EntryPoints?.intoMutable() =
	if (this == null)
		MutableEntryPoints()
	else MutableEntryPoints(this)

class MutableEntryPoints(
	val common: MutableList<EntryPoint> = mutableList(EntryPointCapacity),
	val server: MutableList<EntryPoint> = mutableList(EntryPointCapacity),
	val client: MutableList<EntryPoint> = mutableList(EntryPointCapacity)
)
{
	constructor(immutable: EntryPoints) : this(
		common = immutable.common.toMutableListWithSpaceFor(EntryPointCapacity),
		server = immutable.server.toMutableListWithSpaceFor(EntryPointCapacity),
		client = immutable.client.toMutableListWithSpaceFor(EntryPointCapacity)
	)
	
	fun freeze() =
		EntryPoints(
			common.nullIfEmpty(),
			server.nullIfEmpty(),
			client.nullIfEmpty()
		)
}

data class DependencyContainer(
	val depends   : MutableMap<String, String> = mutableMap(DependencyCapacity),
	val recommends: MutableMap<String, String> = mutableMap(DependencyCapacity),
	val suggests  : MutableMap<String, String> = mutableMap(DependencyCapacity),
	val conflicts : MutableMap<String, String> = mutableMap(DependencyCapacity),
	val breaks    : MutableMap<String, String> = mutableMap(DependencyCapacity)
)
{
	constructor(immutable: FabricMod) : this(
		depends    = immutable.depends   .toMutableMapWithSpaceFor(DependencyCapacity),
		recommends = immutable.recommends.toMutableMapWithSpaceFor(DependencyCapacity),
		suggests   = immutable.suggests  .toMutableMapWithSpaceFor(DependencyCapacity),
		conflicts  = immutable.conflicts .toMutableMapWithSpaceFor(DependencyCapacity),
		breaks     = immutable.breaks    .toMutableMapWithSpaceFor(DependencyCapacity)
	)
}

data class MetadataContainer(
	var name        : String? = null,
	var description : String? = null,
	val authors     : MutableList<Person> = mutableList(PersonCapacity),
	val contributors: MutableList<Person> = mutableList(PersonCapacity),
	val contact     : MutableContactInfo = MutableContactInfo(),
	val license     : String? = null,
	var icon        : Icon?   = null,
)
{
	constructor(immutable: FabricMod) : this(
		name         = immutable.name,
		description  = immutable.description,
		authors      = immutable.authors.peopleIntoMutableList(),
		contributors = immutable.contributors.peopleIntoMutableList(),
		contact      = immutable.contact.intoMutable(),
		license      = immutable.license,
		icon         = immutable.icon
	)
	
	companion object
	{
		internal fun List<Person>?.peopleIntoMutableList() =
			toMutableListWithSpaceFor(PersonCapacity)
	}
}

internal fun ContactInfo?.intoMutable() =
	if (this == null)
		MutableContactInfo()
	else MutableContactInfo(this)

class MutableContactInfo(
	var email   : String? = null,
	var irc     : URI?    = null,
	var homepage: URI?    = null,
	var issues  : URI?    = null,
	var sources : URI?    = null,
	var discord : URI?    = null,
	var slack   : URI?    = null,
	var twitter : URI?    = null,
	val additional: MutableMap<String, URI> = mutableMap(ContactCapacity)
)
{
	constructor(immutable: ContactInfo) : this(
		email    = immutable.email,
		irc      = immutable.irc     ?.toUri(),
		homepage = immutable.homepage?.toUri(),
		issues   = immutable.issues  ?.toUri(),
		discord  = immutable.discord ?.toUri(),
		slack    = immutable.slack   ?.toUri(),
		twitter  = immutable.twitter ?.toUri(),
		additional = immutable.additionalIntoMutableMap()
	)
	
	fun freeze() =
		ContactInfo(
			email,
			irc,
			homepage,
			issues,
			sources,
			discord,
			slack,
			twitter,
			additional
		)
	
	companion object
	{
		@Suppress("NOTHING_TO_INLINE")
		private inline fun String.toUri() = URI(this)
		
		internal fun ContactInfo.additionalIntoMutableMap(): MutableMap<String, URI>
		{
			val additional = additional ?: return mutableMap(ContactCapacity)
			
			val mutable = mutableMap<String, URI>(additional.size + ContactCapacity)
			
			for ((key, value) in additional)
				when (key)
				{
					"discord", "slack", "twitter" -> { }
					else                          ->
						mutable[key] = URI(value)
				}
			
			return mutable
		}
	}
}

class FieldNotSetException(name: String) : Exception(
	message = "The field $name is required but hasn't been set."
)