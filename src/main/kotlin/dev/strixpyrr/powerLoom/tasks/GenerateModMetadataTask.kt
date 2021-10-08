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
package dev.strixpyrr.powerLoom.tasks

import dev.strixpyrr.powerLoom.ModExtension
import dev.strixpyrr.powerLoom.metadata.FabricMod
import dev.strixpyrr.powerLoom.metadata.MutableFabricMod
import okio.buffer
import okio.sink
import okio.source
import okio.use
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import javax.inject.Inject
import kotlin.io.path.div

open class GenerateModMetadataTask @Inject constructor() : DefaultTask()
{
	init
	{
		group = TaskGroup
	}
	
	/**
	 * A [MutableFabricMod] instance containing data to be serialized to a file at
	 * the [outputFile] path. If this is set to null, the task will do nothing.
	 * For instances created by the plugin, this will be set to `mod.metadata` by
	 * default.
	 */
	@get:Input
	@get:Optional
	var modMetadata: MutableFabricMod? = null
	
	/**
	 * The path to a file that replaces values from [modMetadata]. This property is
	 * optional.
	 */
	@get:InputFile
	@get:Optional
	var replacementFile: Path? = null
	
	private var _outputDirectory: Path? = null
	
	/**
	 * The output directory path. This property is required for task instances not
	 * registered by the plugin itself.
	 */
	@get:OutputDirectory
	var outputDirectory: Path
		get() = when (val value = _outputDirectory)
		{
			null -> throw Exception("No output directory was set.")
			else -> value
		}
		set(value)
		{
			_outputDirectory = if (value.isAbsolute)
				value
			else project.projectDir.toPath() / value
		}
	
	/**
	 * The path to the output `fabric.mod.json` file, with [outputDirectory] as its
	 * base.
	 */
	@get:OutputFile
	val outputFile get() = outputDirectory / FabricModJson
	
	internal fun fromSourceSet(sourceSet: KotlinSourceSet)
	{
		val name = sourceSet.name
		
		// Metadata
		
		val mod = project.extensions.getByName(ModExtension.Name) as ModExtension
		
		modMetadata = mod.metadata
		
		// Replacement file
		
		val resourceDirs = sourceSet.resources.sourceDirectories
		
		val jsonFiles = resourceDirs.files.filter { it.name == FabricModJson }
		
		val rf = when
		{
			jsonFiles.isEmpty() -> null
			jsonFiles.size == 1 -> jsonFiles[0].toPath()
			else                ->
			{
				logger.warn(
					"The replacement file path could not be set from the $name " +
					"source set: multiple $FabricModJson files were found in the" +
					" resource directories:"
				)
				
				jsonFiles.forEach { logger.warn("\t$it") }
				
				logger.warn("The replacementFile property will be set to null.")
				
				null
			}
		}
		
		replacementFile = rf
		
		// Output directory
		
		var outputDir  = project.buildDir.toPath()
		outputDir /= "generated-resources/power-loom-$name/"
		
		_outputDirectory = outputDir
		
		// Execution hook
		
		// This will mark the output directory as a source directory, and
		sourceSet.resources.run()
		{
			// Exclude the replacement file from the output.
			if (rf != null) exclude("$rf")
			
			// Add our task's output as a source directory. This will also run our
			// task before its output directory contents are copied.
			srcDir(this@GenerateModMetadataTask)
		}
	}
	
	@TaskAction
	internal fun generate()
	{
		val metadata = mergeMetadata() ?: return
		
		outputFile.sink(CREATE, TRUNCATE_EXISTING)
				  .buffer()
				  .use(metadata::encode)
		
		didWork = true
	}
	
	private fun mergeMetadata(): FabricMod?
	{
		val metadata = modMetadata ?: return null
		
		replacementFile?.run()
		{
			metadata.populateFrom(
				source().buffer().use()
				{
					FabricMod.decode(it)
				}
			)
		}
		
		return metadata.freeze()
	}
	
	companion object
	{
		private const val FabricModJson = "fabric.mod.json"
	}
}
