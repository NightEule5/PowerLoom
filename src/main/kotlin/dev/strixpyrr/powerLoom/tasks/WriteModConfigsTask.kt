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

import dev.strixpyrr.powerLoom.environment.ModConfig
import dev.strixpyrr.powerLoom.environment.SupplementalMod
import dev.strixpyrr.powerLoom.internal.flatAssociateBy
import okio.buffer
import okio.sink
import okio.use
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import javax.inject.Inject

class WriteModConfigsTask @Inject internal constructor() : ManyFileWriteTask()
{
	init
	{
		group = TaskGroup
	}
	
	private lateinit var configs: Map<Path, ModConfig>
	
	override val files get() = configs.keys
	
	internal fun populate(downloadedMods: List<SupplementalMod>)
	{
		configs = downloadedMods.flatAssociateBy(allocation = 2)
		{ m ->
			m.configs.associateBy(
				keySelector = ModConfig::outputPath,
				valueTransform = { mc -> mc }
			)
		}
	}
	
	override fun write(stalePaths: List<Path>) = stalePaths.forEach()
	{
		it.sink(CREATE, TRUNCATE_EXISTING).buffer().use()
		{ sink ->
			contentsAt(it).write(sink)
		}
	}
	
	override fun contentsAt(path: Path) =
		configs[path] ?: error("$UnregisteredConfigPath$path.")
	
	companion object
	{
		private const val UnregisteredConfigPath =
			"The specified path is not a registered config path: "
	}
}