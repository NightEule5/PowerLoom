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
@file:UseSerializers(UriAsStringSerializer::class, IconSerializer::class)

package dev.strixpyrr.powerLoom.metadata

import dev.strixpyrr.powerLoom.metadata.EntryPoint.Adapters.Default
import dev.strixpyrr.powerLoom.metadata.EntryPoint.Adapters.Kotlin
import dev.strixpyrr.powerLoom.metadata.EntryPoint.Adapters.Scala
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.gradle.api.artifacts.Configuration
import java.net.URI
import kotlin.io.path.Path
import java.io.Serializable as JSerializable

private const val EntryPointCapacity = 4
private const val  NestedJarCapacity = 8
private const val      MixinCapacity = 4
private const val    AdapterCapacity = 2
private const val DependencyCapacity = 8
private const val     PersonCapacity = 4
private const val    ContactCapacity = 2

@Serializable
data class MutableFabricMod(
	var id     : String = Unset,
	var version: String = Unset,
	var environment: Environment        = Environment.Either,
	val entryPoints: MutableEntryPoints = MutableEntryPoints(),
	val jars  : MutableList<NestedJar>  = mutableList(NestedJarCapacity),
	val mixins: MutableList<Mixin>      = mutableList(    MixinCapacity),
	val languageAdapters: MutableMap<String, String> = mutableMap(AdapterCapacity),
	val dependencies    : DependencyContainer        = DependencyContainer(),
	val metadata        : MetadataContainer = MetadataContainer(),
	var accessWidener   : String? = null
) : JSerializable
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
		accessWidener    = immutable.accessWidener
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
		
		immutable.accessWidener?.let { accessWidener = it }
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
			accessWidener.toPath()
		)
	}
	
	companion object
	{
		private const val Unset = ""
		
		internal fun String?.toPath() = if (this == null) null else Path(this)
		
		// Serialization
		
		private const val serialVersionUID = 1L
	}
}

internal fun EntryPoints?.intoMutable() =
	if (this == null)
		MutableEntryPoints()
	else MutableEntryPoints(this)

@Serializable
class MutableEntryPoints(
	val common: MutableList<EntryPoint> = mutableList(EntryPointCapacity),
	val server: MutableList<EntryPoint> = mutableList(EntryPointCapacity),
	val client: MutableList<EntryPoint> = mutableList(EntryPointCapacity)
) : JSerializable
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
	
	inline operator fun invoke(populate: MutableEntryPoints.() -> Unit) = this.populate()
	
	fun freeze() =
		EntryPoints(
			common.toSet().nullIfEmpty(),
			server.toSet().nullIfEmpty(),
			client.toSet().nullIfEmpty()
		)
	
	// Shortcuts
	
	inline fun kotlinCommon(block: KotlinEntryPointSpec.() -> Unit) =
		commitCommon(KotlinEntryPointSpec().apply(block))
	
	inline fun kotlinClient(block: KotlinEntryPointSpec.() -> Unit) =
		commitClient(KotlinEntryPointSpec().apply(block))
	
	inline fun kotlinServer(block: KotlinEntryPointSpec.() -> Unit) =
		commitServer(KotlinEntryPointSpec().apply(block))
	
	@PublishedApi
	internal fun commitCommon(spec: KotlinEntryPointSpec) = spec.commit(common)
	
	@PublishedApi
	internal fun commitClient(spec: KotlinEntryPointSpec) = spec.commit(client)
	
	@PublishedApi
	internal fun commitServer(spec: KotlinEntryPointSpec) = spec.commit(server)
	
	fun kotlinCommon(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = common(packageName, className, functionName, adapter = Kotlin)
	
	fun scalaCommon(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = common(packageName, className, functionName, adapter = Scala)
	
	fun common(
		 packageName: String,
		   className: String,
		functionName: String = "",
		adapter     : String = Default
	)
	{
		common += toEp(packageName, className, functionName, adapter)
	}
	
	fun kotlinClient(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = client(packageName, className, functionName, adapter = Kotlin)
	
	fun scalaClient(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = client(packageName, className, functionName, adapter = Scala)
	
	fun client(
		 packageName: String,
		   className: String,
		functionName: String = "",
		adapter     : String = Default
	)
	{
		client += toEp(packageName, className, functionName, adapter)
	}
	
	fun kotlinServer(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = server(packageName, className, functionName, adapter = Kotlin)
	
	fun scalaServer(
		 packageName: String,
		   className: String,
		functionName: String = ""
	) = server(packageName, className, functionName, adapter = Scala)
	
	fun server(
		 packageName: String,
		   className: String,
		functionName: String = "",
		adapter     : String = Default
	)
	{
		server += toEp(packageName, className, functionName, adapter)
	}
	
	class KotlinEntryPointSpec @PublishedApi internal constructor()
	{
		private var `package` = ""
		private var `class`   = ""
		private var function  = ""
		
		infix fun inPackage(packageName: String) = apply { this.`package` = packageName }
		
		infix fun inClass(className: String) = apply { this.`class` = className }
		
		infix fun inCompanionOf(className: String) = inClass("$className\$Companion")
		
		infix fun inFile(fileName: String) = inClass("${fileName}Kt")
		
		infix fun function(functionName: String) = apply { this.function = functionName }
		
		@PublishedApi
		internal fun commit(list: MutableList<EntryPoint>) =
			toEp(
				`package`.ifEmpty { throw IllegalArgumentException(EmptyPackage) },
				`class`  .ifEmpty { throw IllegalArgumentException(EmptyClass  ) },
				function,
				adapter = Kotlin
			).also { list += it }
		
		private companion object
		{
			private const val EmptyPackage =
				"The Package cannot be empty. Invoke inPackage to set the package."
			private const val EmptyClass   =
				"The Class cannot be empty. Invoke inClass, inCompanionOf, or " +
				"inFile to set the class."
		}
	}
	
	companion object
	{
		// Serialization
		
		private const val serialVersionUID = 1L
	}
}

internal fun toEp(
	 packageName: String,
	   className: String,
	functionName: String,
	adapter     : String
) = EntryPoint(
		formatEpPath(
			 packageName,
			   className,
			functionName
		), adapter
	)

internal fun formatEpPath(
	 packageName: String,
	   className: String,
	functionName: String
) = if (functionName.isEmpty())
		"$packageName.$className"
	else "$packageName.$className::$functionName"

@Serializable
data class DependencyContainer(
	val depends   : MutableMap<String, List<String>> = mutableMap(DependencyCapacity),
	val recommends: MutableMap<String, List<String>> = mutableMap(DependencyCapacity),
	val suggests  : MutableMap<String, List<String>> = mutableMap(DependencyCapacity),
	val conflicts : MutableMap<String, List<String>> = mutableMap(DependencyCapacity),
	val breaks    : MutableMap<String, List<String>> = mutableMap(DependencyCapacity)
) : JSerializable
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
	
	fun populate(configuration: Configuration) = populateFrom(configuration)
	
	inline operator fun invoke(populate: DependencyContainer.() -> Unit) = this.populate()
	
	// Shortcuts
	
	fun dependOn(modId: String, version: String) = dependOn(modId, listOf(version))
	
	fun dependOn(modId: String, vararg versions: String) = dependOn(modId, listOf(*versions))
	
	fun dependOn(modId: String, versions: List<String>) = depends.add(modId, versions)
	
	fun recommend(modId: String, version: String) = recommend(modId, listOf(version))
	
	fun recommend(modId: String, vararg versions: String) = recommend(modId, listOf(*versions))
	
	fun recommend(modId: String, versions: List<String>) = recommends.add(modId, versions)
	
	fun suggest(modId: String, version: String) = suggest(modId, listOf(version))
	
	fun suggest(modId: String, vararg versions: String) = suggest(modId, listOf(*versions))
	
	fun suggest(modId: String, versions: List<String>) = suggests.add(modId, versions)
	
	fun specifyConflict(modId: String, version: String) = specifyConflict(modId, listOf(version))
	
	fun specifyConflict(modId: String, vararg versions: String) = specifyConflict(modId, listOf(*versions))
	
	fun specifyConflict(modId: String, versions: List<String>) = conflicts.add(modId, versions)
	
	fun specifyBreak(modId: String, version: String) = specifyBreak(modId, listOf(version))
	
	fun specifyBreak(modId: String, vararg versions: String) = specifyBreak(modId, listOf(*versions))
	
	fun specifyBreak(modId: String, versions: List<String>) = breaks.add(modId, versions)
	
	private fun MutableMap<String, List<String>>.add(modId: String, versions: List<String>)
	{
		this[modId] = when (val existingVersions = this[modId])
		{
			null -> versions
			else -> existingVersions + versions
		}
	}
	
	companion object
	{
		// Serialization
		
		private const val serialVersionUID = 1L
	}
}

@Serializable
data class MetadataContainer(
	var name        : String? = null,
	var description : String? = null,
	val authors     : MutableList<Person> = mutableList(PersonCapacity),
	val contributors: MutableList<Person> = mutableList(PersonCapacity),
	val contact     : MutableContactInfo = MutableContactInfo(),
	var license     : String? = null,
	var icon        : Icon?   = null,
) : JSerializable
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
		immutable.name       ?.let { name        = it }
		immutable.description?.let { description = it }
		immutable.license    ?.let { license     = it }
		immutable.icon       ?.let { icon        = it }
		
		populatePeople(immutable)
		
		immutable.contact?.let(contact::populateWith)
	}
	
	private fun populatePeople(immutable: FabricMod)
	{
		immutable.authors     ?.let { authors      += it }
		immutable.contributors?.let { contributors += it }
	}
	
	inline operator fun invoke(populate: MetadataContainer.() -> Unit) = this.populate()
	
	// Shortcuts
	
	fun author(name: String                          ) { authors += Person(name             ) }
	fun author(name: String, contactInfo: ContactInfo) { authors += Person(name, contactInfo) }
	
	inline fun author(name: String, populate: MutableContactInfo.() -> Unit) =
		author(name, MutableContactInfo().apply(populate).freeze())
	
	fun contributor(name: String                          ) { contributors += Person(name             ) }
	fun contributor(name: String, contactInfo: ContactInfo) { contributors += Person(name, contactInfo) }
	
	inline fun contributor(name: String, populate: MutableContactInfo.() -> Unit) =
		contributor(name, MutableContactInfo().apply(populate).freeze())
	
	companion object
	{
		// Serialization
		
		private const val serialVersionUID = 1L
	}
}

internal fun ContactInfo?.intoMutable() =
	if (this == null)
		MutableContactInfo()
	else MutableContactInfo(this)

@Serializable
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
) : JSerializable
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
		immutable.email   ?.let { email    = it         }
		immutable.irc     ?.let { irc      = it.toUri() }
		immutable.homepage?.let { homepage = it.toUri() }
		immutable.issues  ?.let { issues   = it.toUri() }
		immutable.sources ?.let { sources  = it.toUri() }
		
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
	
	inline operator fun invoke(populate: MutableContactInfo.() -> Unit) = this.populate()
	
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
		
		// Serialization
		
		private const val serialVersionUID = 1L
	}
}

class FieldNotSetException(name: String) : Exception(
	"The field $name is required but hasn't been set."
)

// Serialization

internal object UriAsStringSerializer : KSerializer<URI>
{
	override val descriptor = PrimitiveSerialDescriptor("URI", STRING)
	
	override fun deserialize(decoder: Decoder) = URI(decoder.decodeString())
	
	override fun serialize(encoder: Encoder, value: URI) = encoder.encodeString("$value")
}
