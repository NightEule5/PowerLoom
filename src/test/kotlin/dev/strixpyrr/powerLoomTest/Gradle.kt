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
package dev.strixpyrr.powerLoomTest

import okio.*
import okio.HashingSource.Companion.sha1
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.GradleRunner.create
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.notExists

internal object Gradle
{
	@JvmStatic
	fun createGradle(
		projectPath: Path,
		targetTask: String,
		arguments: List<String> = emptyList()
	) = GradleBuild(rootPath / projectPath, targetTask, arguments)
	
	@JvmStatic
	internal fun commitIfNewer(root: Path, name: String, buffer: Buffer)
	{
		(root / name).run()
		{
			if (isNewer(buffer)) openFile().use(buffer::readAll)
		}
	}
	
	@JvmStatic
	private fun Path.isNewer(buffer: Buffer) =
		notExists() || sha1(source()).use { it.hash } != buffer.sha1()
	
	@JvmStatic
	private fun Path.openFile() = createParents().sink(CREATE, TRUNCATE_EXISTING)
	
	@JvmStatic
	private fun Path.createParents() = apply { parent.createDirectories() }
	
	@JvmStatic
	infix fun BuildResult.outcomeOf(path: String) = task(path)?.outcome
	
	@JvmStatic private val rootPath = Path("run")
	
	private const val CurrentVersion = "7.2"
	
	class GradleBuild private constructor(
		private val gr: GradleRunner,
		private val projectDir: Path
	)
	{
		fun run(): BuildResult = gr.build()
		
		constructor(projectDir: Path, task: String, arguments: List<String>) : this(
			create().withGradleVersion(CurrentVersion)
					.withProjectDir(projectDir.toFile())
					.withPluginClasspath()
					.withArguments(
						listOf(task) + arguments
					)
					.forwardOutput(),
			projectDir
		)
		
		fun intoBuild(buffer: Buffer) =
			commitIfNewer(
				root = projectDir,
				name = "build.gradle.kts",
				buffer
			)
		
		inline fun intoBuild(block: BufferedSink.() -> Unit) = intoBuild(Buffer().apply(block))
		
		fun intoSettings(buffer: Buffer) =
			commitIfNewer(
				root = projectDir,
				name = "settings.gradle.kts",
				buffer
			)
		
		inline fun intoSettings(block: BufferedSink.() -> Unit) = intoSettings(Buffer().apply(block))
		
		fun intoResourceAt(name: String, buffer: Buffer) =
			commitIfNewer(
				root = projectDir / resPath,
				name,
				buffer
			)
		
		inline fun intoResourceAt(
			name: String,
			block: BufferedSink.() -> Unit
		) = this.intoResourceAt(name, Buffer().apply(block))
		
		fun intoProperties(buffer: Buffer) =
			commitIfNewer(
				root = projectDir,
				name = "gradle.properties",
				buffer
			)
		
		inline fun intoProperties(block: BufferedSink.() -> Unit) = intoProperties(Buffer().apply(block))
		
		companion object
		{
			@JvmStatic private val resPath = Path("src/main/resources")
		}
	}
}