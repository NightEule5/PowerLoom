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
@file:OptIn(ExperimentalSerializationApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package dev.strixpyrr.powerLoom.metadata

import kotlinx.serialization.*
import kotlinx.serialization.EncodeDefault.Mode.ALWAYS
import kotlinx.serialization.json.*
import okio.BufferedSink
import okio.BufferedSource
import java.net.URI
import java.nio.file.Path


// Mostly implements the spec:
// https://fabricmc.net/wiki/documentation:fabric_mod_json_spec
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class FabricMod internal constructor(
	@EncodeDefault(mode = ALWAYS)
	val schemaVersion   : Int = 1,
	val id              : String,
	val version         : String,
	val environment     : Environment = Environment.Either,
	@SerialName("entrypoints")
	val entryPoints     : EntryPoints?,
	val jars            : Set<NestedJar>?,
	val mixins          : Set<Mixin>?,
	val languageAdapters: Map<String, String>?,
	val depends         : Map<String, List<String>>?,
	val recommends      : Map<String, List<String>>?,
	val suggests        : Map<String, List<String>>?,
	val conflicts       : Map<String, List<String>>?,
	val breaks          : Map<String, List<String>>?,
	val name            : String?,
	val description     : String?,
	val authors         : List<Person>?,
	val contributors    : List<Person>?,
	@Serializable(with = ContactInfoSerializer::class)
	val contact         : ContactInfo?,
	val license         : String?,
	@Serializable(with = IconSerializer::class)
	val icon            : Icon?,
	val accessWidener   : String?
	// val custom : Any?
)
{
	constructor(
		id              : String,
		version         : String,
		environment     : Environment               = Environment.Either,
		entryPoints     : EntryPoints               = EntryPoints(),
		jars            : Set<NestedJar>            = emptySet(),
		mixins          : Set<Mixin>                = emptySet(),
		languageAdapters: Map<String, String>       = emptyMap(),
		depends         : Map<String, List<String>> = emptyMap(),
		recommends      : Map<String, List<String>> = emptyMap(),
		suggests        : Map<String, List<String>> = emptyMap(),
		conflicts       : Map<String, List<String>> = emptyMap(),
		breaks          : Map<String, List<String>> = emptyMap(),
		name            : String?                   = null,
		description     : String?                   = null,
		authors         : Set<Person>               = emptySet(),
		contributors    : Set<Person>               = emptySet(),
		contact         : ContactInfo               = ContactInfo(),
		license         : String?                   = null,
		icon            : Icon?                     = null,
		accessWidener   : Path?                     = null
	) : this(
		schemaVersion = 1,
		id,
		version,
		environment,
		entryPoints.notEmptyOrNull(),
		jars            .ifEmpty { null },
		mixins          .ifEmpty { null },
		languageAdapters.ifEmpty { null },
		depends         .ifEmpty { null },
		recommends      .ifEmpty { null },
		suggests        .ifEmpty { null },
		conflicts       .ifEmpty { null },
		breaks          .ifEmpty { null },
		name,
		description,
		authors         .ifEmpty { null }?.map(Person::trimEmptyContact),
		contributors    .ifEmpty { null }?.map(Person::trimEmptyContact),
		contact.notEmptyOrNull(),
		license,
		icon,
		if (accessWidener != null) "$accessWidener" else null
	)
	{
		require(id matches IdRegex, ::ModIdInvalid)
	}
	
	fun encode(sink: BufferedSink) =
		json.encodeToStream(value = this, stream = sink.outputStream())
	
	companion object
	{
		// Language = Regex
		private const val IdRegexPattern = "^[a-z][a-z0-9-_]{1,63}$"
		
		@JvmField
		internal val IdRegex = Regex(IdRegexPattern)
		
		private const val ModIdInvalid =
			"The mod identifier is invalid. It must be consist of at least one " +
			"alphanumeric character (dashes and underscores included) following" +
			" one lowercase letter, to a maximum of 64 characters total; it mus" +
			"t satisfy the regex /$IdRegexPattern/."
		
		@JvmStatic
		private val json = Json { explicitNulls  = false }
		
		@JvmStatic
		fun decode(source: BufferedSource) =
			json.decodeFromStream<FabricMod>(stream = source.inputStream())
	}
}

@Serializable
enum class Environment
{
	@SerialName("*"     ) Either,
	@SerialName("client") Client,
	@SerialName("server") Server
}

@Serializable
data class EntryPoint(
	val value  : String,
	val adapter: String = Adapters.Default
)
{
	init
	{
		require(value.isNotEmpty()) { "The entry point value cannot be empty." }
	}
	
	object Adapters
	{
		const val Default = "default"
		const val Kotlin  = "kotlin"
		const val Scala   = "scala"
	}
}

@Serializable
data class EntryPoints(
	@SerialName("main")
	val common: Set<EntryPoint>? = null,
	val server: Set<EntryPoint>? = null,
	val client: Set<EntryPoint>? = null
)
{
	private val isEmpty get() =
		common.isNullOrEmpty() &&
		server.isNullOrEmpty() &&
		client.isNullOrEmpty()
	
	// Todo: This could cause empty arrays in the file that should be omitted.
	fun notEmptyOrNull() = if (isEmpty) null else this
}

@Serializable
data class NestedJar(val file: String)
{
	constructor(file: Path) : this("$file")
}

@Serializable
data class Mixin(
	val config     : String,
	val environment: Environment = Environment.Either
)
{
	constructor(
		config     : Path,
		environment: Environment = Environment.Either
	) : this("$config", environment)
}

@Serializable
data class Person(
	val name: String,
	@Serializable(with = ContactInfoSerializer::class)
	val contact: ContactInfo? = null
)
{
	fun trimEmptyContact() = when (val contact = contact)
	{
		null -> this
		else ->
			if (contact.isEmpty)
				copy(name = name, contact = null)
			else this
	}
}

@Serializable
data class ContactInfo internal constructor(
	val email   : String? = null,
	val irc     : String? = null,
	val homepage: String? = null,
	val issues  : String? = null,
	val sources : String? = null,
	val additional: Map<String, String>? = null
)
{
	constructor(
		email   : String? = null,
		irc     : URI?    = null,
		homepage: URI?    = null,
		issues  : URI?    = null,
		sources : URI?    = null,
		discord : URI?    = null,
		slack   : URI?    = null,
		twitter : URI?    = null,
		vararg additional: Pair<String, URI>
	) : this(
		email,
		irc,
		homepage,
		issues,
		sources,
		discord,
		slack,
		twitter,
		mapOf(*additional)
	)
	
	@OptIn(ExperimentalStdlibApi::class)
	constructor(
		email   : String? = null,
		irc     : URI?    = null,
		homepage: URI?    = null,
		issues  : URI?    = null,
		sources : URI?    = null,
		discord : URI?    = null,
		slack   : URI?    = null,
		twitter : URI?    = null,
		additional: Map<String, URI> = emptyMap()
	) : this(
		email,
		if (irc      != null) "$irc"      else null,
		if (homepage != null) "$homepage" else null,
		if (issues   != null) "$issues"   else null,
		if (sources  != null) "$sources"  else null,
		additional = buildMap(3 + additional.size)
		{
			if (discord != null) this["discord"] = "$discord"
			if (slack   != null) this["slack"  ] = "$slack"
			if (twitter != null) this["twitter"] = "$twitter"
			
			additional.mapValuesTo(this) { (_, v) -> "$v" }
		}
	)
	{
		require(irc == null || irc.scheme.isIrcScheme)
		{
			"IRC must be an IRC, IRCS, or IRC6 URL."
		}
		
		require(homepage == null || homepage.scheme.isHttpScheme)
		{
			"A homepage must be an HTTP(S) URL."
		}
		
		require(issues == null || issues.scheme.isHttpScheme)
		{
			"An issue page must be an HTTP(S) URL."
		}
		
		require(sources == null || sources.scheme.isVcsScheme)
		{
			"A sources link must be a Git, SSH, FTP(S), or HTTP(S) URL."
		}
		
		require(discord == null || discord.isValidDiscordInvite)
		{
			"A Discord link must be a valid server invite URL."
		}
		
		require(slack == null || slack.isValidSlack)
		{
			"A Slack link must consist of a subdomain of slack.com."
		}
		
		require(twitter == null || twitter.isValidTwitterProfile)
		{
			"A Twitter link must be a valid Twitter profile URL."
		}
	}
	
	internal val discord get() = additional?.get("discord")
	internal val slack   get() = additional?.get("slack"  )
	internal val twitter get() = additional?.get("twitter")
	
	val isEmpty get() =
		email     .isNullOrEmpty() &&
		irc       .isNullOrEmpty() &&
		homepage  .isNullOrEmpty() &&
		issues    .isNullOrEmpty() &&
		sources   .isNullOrEmpty() &&
		additional.isNullOrEmpty()
	
	fun notEmptyOrNull() = if (isEmpty) null else this
	
	companion object
	{
		internal val knownKeys get() =
			arrayOf("email", "irc", "homepage", "issues", "sources")
		
		internal val String.isIrcScheme get() = when (this)
		{
			"irc", "ircs", "irc6" -> true
			else                  -> false
		}
		
		internal val String.isHttpScheme get() = when (this)
		{
			"http", "https" -> true
			else            -> false
		}
		
		internal val String.isVcsScheme get() = when (this)
		{
			"git",
			"ssh",
			"ftp",
			"ftps",
			"http",
			"https" -> true
			else    -> false
		}
		
		internal val URI.isValidDiscordInvite  get() = isValid("discord.gg" )
		internal val URI.isValidTwitterProfile get() = isValid("twitter.com")
		internal val URI.isValidSlack get() =
			scheme.isHttpScheme &&
			host.endsWith(".slack.com")
		
		private fun URI.isValid(targetHost: String) =
			scheme.isHttpScheme &&
			host == targetHost  &&
			path != null
	}
}

@Serializable
data class Icon internal constructor(
	val path: String? = null,
	val paths: Map<String, String>? = null
)
{
	constructor(path: Path) : this("$path")
	
	constructor(path: String) : this(path, paths = null)
	
	constructor(paths: Map<String, String>) : this(path = null, paths)
	{
		for (key in paths.keys)
			require(key matches KeyRegex)
			{
				"An icon key must be the icon's width: [$key] is not a valid key."
			}
	}
	
	companion object
	{
		private val KeyRegex = Regex("^[1-9][0-9]*$")
		
		// Not a valid constructor because Java doesn't have reified generics. >:|
		@JvmStatic
		operator fun invoke(paths: Map<String, Path>) =
			Icon(paths.mapValues { "$it" })
	}
}

// Serialization Sorcery

@Serializer(forClass = ContactInfo::class)
internal object ContactInfoSerializer : AdditionalPropertySerializer<ContactInfo>(
	ContactInfo.serializer(),
	ContactInfo.knownKeys
)

internal open class AdditionalPropertySerializer<T : Any>(
	parent: KSerializer<T>, private val knownKeys: Array<String>
) : JsonTransformingSerializer<T>(parent)
{
	override fun transformSerialize(element: JsonElement): JsonElement
	{
		if (element !is JsonObject || "additional" !in element) return element
		
		val additional = element["additional"] as JsonObject
		
		if (additional.isEmpty()) return element
		
		val elements = (element + additional) as LinkedHashMap
		
		elements -= "additional"
		
		return JsonObject(elements)
	}
	
	override fun transformDeserialize(element: JsonElement): JsonElement
	{
		if (element !is JsonObject || element.keys in knownKeys) return element
		
		val additional = LinkedHashMap<String, JsonElement>(element.size)
		val known      = LinkedHashMap<String, JsonElement>(element.size)
		
		element.filterTo(known)
		{ (key, value) ->
			if (key in knownKeys)
				true
			else
			{
				additional[key] = value
				
				false
			}
		}
		
		known["additional"] = JsonObject(additional)
		
		return JsonObject(known)
	}
	
	companion object
	{
		private operator fun <T> Array<T>.contains(subset: Set<T>) =
			subset.all { it in this }
	}
}

internal object IconSerializer : OneOfSerializer<Icon>(Icon.serializer())
{
	override fun transformDeserialize(element: JsonElement) =
		JsonObject(
			mapOf(
				when (element)
				{
					is JsonPrimitive ->
					{
						require(element.isString) { "Unknown type." }
						
						"path" to element
					}
					is JsonObject    -> "paths" to element
					else             -> error("Unknown type.")
				}
			)
		)
}

internal abstract class OneOfSerializer<T : Any>(
	parent: KSerializer<T>
) : JsonTransformingSerializer<T>(parent)
{
	override fun transformSerialize(element: JsonElement): JsonElement
	{
		check(element is JsonObject)
		
		for (value in element.values)
			if (value !== JsonNull)
				return value
		
		error("No values")
	}
}
