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

import dev.strixpyrr.powerLoom.metadata.FabricMod
import dev.strixpyrr.powerLoomTest.Gradle.rootPath
import io.kotest.matchers.paths.shouldExist
import okio.buffer
import okio.source
import okio.use
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div

private val MetadataOutputPath =
	Path(
		"build",
		"generated-resources",
		"power-loom-main",
		"fabric.mod.json"
	)

internal fun deleteMetadataOutput(projectRoot: Path) =
	(rootPath / projectRoot / MetadataOutputPath).deleteIfExists()

internal fun metadataOutput(projectRoot: Path) =
	(rootPath / projectRoot / MetadataOutputPath).let()
	{ path ->
		path.shouldExist()
		
		path.source().buffer().use { FabricMod.decode(it) }
	}