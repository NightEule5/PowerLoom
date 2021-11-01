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

package dev.strixpyrr.powerLoom.environment

import com.charleskorn.kaml.Yaml
import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.toml.TomlWriter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.properties.Properties
import okio.BufferedSink
import okio.Source
import okio.source
import okio.use
import java.io.IOException
import java.io.Writer
import java.nio.file.Path

sealed class ModConfig(
	internal val outputPath: Path
)
{
	internal abstract fun write(sink: BufferedSink)
}

internal class StringModConfig(
	outputPath: Path,
	private val text: String
) : ModConfig(outputPath)
{
	override fun write(sink: BufferedSink) = write(text, sink)
}

internal class ResourceModConfig(
	outputPath: Path,
	private val resourcePath: Path,
	private val target: Class<*>
) : ModConfig(outputPath)
{
	override fun write(sink: BufferedSink) = write(resourcePath, sink, target)
}

internal sealed class SerializingModConfig<V : Any>(
	outputPath: Path,
	@JvmField protected val serializer: KSerializer<V>?,
	@JvmField protected val value: V
) : ModConfig(outputPath)
{
	companion object
	{
		@JvmStatic
		fun <V : Any> json(
			outputPath: Path,
			serializer: KSerializer<V>,
			value: V,
			json: Json
		): SerializingModConfig<V> =
			JsonModConfig(outputPath, serializer, value, json)
		
		@JvmStatic
		fun <V : Any> properties(
			outputPath: Path,
			serializer: KSerializer<V>,
			value: V,
			properties: Properties
		): SerializingModConfig<V> =
			PropertiesModConfig(outputPath, serializer, value, properties)
		
		@JvmStatic
		fun <V : Any> yaml(
			outputPath: Path,
			serializer: KSerializer<V>,
			value: V,
			yaml: Yaml
		): SerializingModConfig<V> =
			YamlModConfig(outputPath, serializer, value, yaml)
		
		@JvmStatic
		fun <V : ITomlConfig> toml(
			outputPath: Path,
			value: V
		): SerializingModConfig<V> =
			TomlModConfig(outputPath, value)
	}
}

private class JsonModConfig<V : Any>(
	outputPath: Path,
	serializer: KSerializer<V>,
	value: V,
	private val json: Json
) : SerializingModConfig<V>(outputPath, serializer, value)
{
	override fun write(sink: BufferedSink) =
		enc(value, serializer as KSerializer<V>, sink, json)
}

private class PropertiesModConfig<V : Any>(
	outputPath: Path,
	serializer: KSerializer<V>,
	value: V,
	private val properties: Properties
) : SerializingModConfig<V>(outputPath, serializer, value)
{
	override fun write(sink: BufferedSink) =
		enc(value, serializer as KSerializer<V>, sink, properties)
}

private class YamlModConfig<V : Any>(
	outputPath: Path,
	serializer: KSerializer<V>,
	value: V,
	private val yaml: Yaml
) : SerializingModConfig<V>(outputPath, serializer, value)
{
	override fun write(sink: BufferedSink) =
		enc(value, serializer as KSerializer<V>, sink, yaml)
}

private class TomlModConfig<V : ITomlConfig>(
	outputPath: Path,
	value: V
) : SerializingModConfig<V>(outputPath, null, value)
{
	override fun write(sink: BufferedSink) = enc(value, sink)
}

internal fun write(value: String, sink: BufferedSink) { sink.writeUtf8(value) }

internal fun write(resource: Path, sink: BufferedSink, target: Class<*>)
{
	openResource(resource, target).use(sink::writeAll)
}

private fun openResource(resource: Path, target: Class<*>): Source
{
	val stream =
		target.getResourceAsStream("$resource") ?:
		throw IOException(
			"A resource with path $resource could not be found."
		)
	
	return stream.source()
}

internal fun <V : Any> enc(
	value: V,
	serializer: KSerializer<V>,
	sink: BufferedSink,
	json: Json
)
{
	json.encodeToStream(serializer, value, sink.outputStream())
}

internal fun <V : Any> enc(
	value: V,
	serializer: KSerializer<V>,
	sink: BufferedSink,
	properties: Properties
)
{
	val map = properties.encodeToStringMap(serializer, value)
	
	for ((key, str) in map)
		sink.writeUtf8(key)
			.writeUtf8CodePoint('='.code)
			.writeUtf8(str)
			.writeUtf8CodePoint('\n'.code)
}

internal fun <V : Any> enc(
	value: V,
	serializer: KSerializer<V>,
	sink: BufferedSink,
	yaml: Yaml
)
{
	sink.writeUtf8(
		yaml.encodeToString(serializer, value)
	)
}

internal fun enc(
	value: ITomlConfig,
	sink: BufferedSink
)
{
	class SinkWriter(
		private val output: BufferedSink
	) : Writer()
	{
		override fun flush() = output.flush()
		override fun close() = output.close()
		
		override fun write(c: Int)
		{
			output.writeUtf8CodePoint(c)
		}
		
		override fun write(cbuf: CharArray)
		{
			output.writeUtf8(String(cbuf))
		}
		
		override fun write(cbuf: CharArray, off: Int, len: Int)
		{
			output.writeUtf8(String(cbuf, off, len))
		}
		
		override fun write(str: String)
		{
			output.writeUtf8(str)
		}
		
		override fun write(str: String, off: Int, len: Int)
		{
			output.writeUtf8(str, off, off + len)
		}
	}
	
	val config = CommentedConfig.inMemory()
	
	value.copyInto(config)
	
	TomlWriter().write(config, SinkWriter(output = sink))
}

interface ITomlConfig
{
	fun copyInto(config: CommentedConfig)
}