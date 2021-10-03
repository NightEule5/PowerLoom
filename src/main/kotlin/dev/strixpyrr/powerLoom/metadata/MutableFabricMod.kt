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
		jars   = mutableList(immutable.jars   sizeOr 0 + NestedJarCapacity),
		mixins = mutableList(immutable.mixins sizeOr 0 +     MixinCapacity),
		languageAdapters = mutableMap(immutable.languageAdapters sizeOr 0 + AdapterCapacity),
		dependencies     = DependencyContainer(immutable),
		metadata         =   MetadataContainer(immutable),
		accessWidener    = immutable.accessWidener.toPath()
	)
	
	fun populateFrom(immutable: FabricMod) = this()
	{
		id      = immutable.id
		version = immutable.version
		environment = immutable.environment
		
		immutable.entryPoints     ?.let(entryPoints::populateWith)
		immutable.jars            ?.let { jars             += it }
		immutable.mixins          ?.let { mixins           += it }
		immutable.languageAdapters?.let { languageAdapters += it }
		
		dependencies.populateWith(immutable)
		metadata    .populateWith(immutable)
		
		immutable.accessWidener?.let { accessWidener = it.toPath() }
	}
	
	inline operator fun invoke(populate: MutableFabricMod.() -> Unit) = this.populate()
	
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
			jars  .toSet(),
			mixins.toSet(),
			languageAdapters,
			depends,
			recommends,
			suggests,
			conflicts,
			breaks,
			name,
			description,
			authors     .toSet(),
			contributors.toSet(),
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
		common = mutableList(immutable.common sizeOr EntryPointCapacity),
		server = mutableList(immutable.server sizeOr EntryPointCapacity),
		client = mutableList(immutable.client sizeOr EntryPointCapacity)
	)
	{
		populateWith(immutable)
	}
	
	fun populateWith(immutable: EntryPoints)
	{
		immutable.common?.let { common += it }
		immutable.server?.let { server += it }
		immutable.client?.let { client += it }
	}
	
	fun freeze() =
		EntryPoints(
			common.toSet().nullIfEmpty(),
			server.toSet().nullIfEmpty(),
			client.toSet().nullIfEmpty()
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
		depends    = mutableMap(immutable.depends    sizeOr DependencyCapacity),
		recommends = mutableMap(immutable.recommends sizeOr DependencyCapacity),
		suggests   = mutableMap(immutable.suggests   sizeOr DependencyCapacity),
		conflicts  = mutableMap(immutable.conflicts  sizeOr DependencyCapacity),
		breaks     = mutableMap(immutable.breaks     sizeOr DependencyCapacity)
	)
	{
		populateWith(immutable)
	}
	
	fun populateWith(immutable: FabricMod)
	{
		immutable.depends   ?.let { depends    += it }
		immutable.recommends?.let { recommends += it }
		immutable.suggests  ?.let { suggests   += it }
		immutable.conflicts ?.let { conflicts  += it }
		immutable.breaks    ?.let { breaks     += it }
	}
}

data class MetadataContainer(
	var name        : String? = null,
	var description : String? = null,
	val authors     : MutableList<Person> = mutableList(PersonCapacity),
	val contributors: MutableList<Person> = mutableList(PersonCapacity),
	val contact     : MutableContactInfo = MutableContactInfo(),
	var license     : String? = null,
	var icon        : Icon?   = null,
)
{
	constructor(immutable: FabricMod) : this(
		name         = immutable.name,
		description  = immutable.description,
		authors      = mutableList(immutable.authors      sizeOr 0 + PersonCapacity),
		contributors = mutableList(immutable.contributors sizeOr 0 + PersonCapacity),
		contact      = immutable.contact.intoMutable(),
		license      = immutable.license,
		icon         = immutable.icon
	)
	{
		populatePeople(immutable)
	}
	
	fun populateWith(immutable: FabricMod)
	{
		name        = immutable.name
		description = immutable.description
		license     = immutable.license
		icon        = immutable.icon
		
		populatePeople(immutable)
		
		immutable.contact?.let(contact::populateWith)
	}
	
	private fun populatePeople(immutable: FabricMod)
	{
		immutable.authors     ?.let { authors      += it }
		immutable.contributors?.let { contributors += it }
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
	private constructor(additionalCapacity: Int) : this(
		additional = mutableMap(additionalCapacity)
	)
	
	constructor(immutable: ContactInfo) : this(
		immutable.additional sizeOr 0 + ContactCapacity
	)
	{
		populateWith(immutable)
	}
	
	fun populateWith(immutable: ContactInfo)
	{
		email    = immutable.email
		irc      = immutable.irc     ?.toUri()
		homepage = immutable.homepage?.toUri()
		issues   = immutable.issues  ?.toUri()
		sources  = immutable.sources ?.toUri()
		
		val additional = additional
		
		immutable.additional?.let()
		{
			for ((key, value) in it)
				when (key)
				{
					"discord" -> discord = value.toUri()
					"slack"   -> slack   = value.toUri()
					"twitter" -> twitter = value.toUri()
					else      ->
						additional[key] = value.toUri()
				}
		}
	}
	
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
	}
}

class FieldNotSetException(name: String) : Exception(
	"The field $name is required but hasn't been set."
)