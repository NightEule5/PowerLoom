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

import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.use
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.GradleRunner.create
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

internal object Gradle
{
	@JvmStatic
	fun createGradle(projectPath: Path) = GradleBuild(rootPath / projectPath)
	
	@JvmStatic
	internal fun openScript(root: Path, name: String) =
		(root / name).createParents().sink(CREATE, TRUNCATE_EXISTING).buffer()
	
	@JvmStatic
	private fun Path.createParents() = apply { parent.createDirectories() }
	
	private val rootPath = Path("run")
	
	private const val CurrentVersion = "7.2"
	
	class GradleBuild private constructor(
		private val gr: GradleRunner,
		private val projectDir: Path
	)
	{
		fun run(): BuildResult = gr.build()
		
		constructor(projectDir: Path) : this(
			create().withGradleVersion(CurrentVersion)
					.withProjectDir(projectDir.toFile())
					.withPluginClasspath(),
			projectDir
		)
		
		fun openBuild() =
			openScript(
				root = projectDir,
				name = "build.gradle.kts"
			)
		
		inline fun intoBuild(block: BufferedSink.() -> Unit) = openBuild().use(block)
		
		fun openSettings() =
			openScript(
				root = projectDir,
				name = "settings.gradle.kts"
			)
		
		inline fun intoSettings(block: BufferedSink.() -> Unit) = openSettings().use(block)
	}
}