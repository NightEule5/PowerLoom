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

import dev.strixpyrr.powerLoom.internal.hashSha256
import okio.ByteString
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import kotlin.io.path.notExists

sealed class ManyFileWriteTask : DefaultTask()
{
	@get:OutputFiles
	abstract val files: Set<Path>
	
	private lateinit var _contentHashes: Map<String, ByteString>
	
	@get:Input
	internal val contentHashes get() =
		if (::_contentHashes.isInitialized)
			_contentHashes
		else
		{
			val hashes = files.associateBy(
				keySelector = Path::toString,
				valueTransform =
				{
					contentsAt(it).hash()
				}
			)
			
			_contentHashes = hashes
			
			hashes
		}
	
	@TaskAction
	internal fun writeFiles()
	{
		val hashes = contentHashes
		val stale  = files.filter { it.isStale(hashes) }
		
		if (stale.isEmpty())
		{
			didWork = false
			
			return
		}
		
		write(stale)
		
		didWork = true
	}
	
	protected abstract fun write(stalePaths: List<Path>)
	
	protected abstract fun contentsAt(path: Path): IHashable
	
	companion object
	{
		@JvmStatic
		private fun Path.isStale(hashes: Map<String, ByteString>) =
			notExists() || hashSha256() == hashes["$this"]
	}
}
