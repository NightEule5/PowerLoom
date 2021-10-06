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

import okio.buffer
import okio.source
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

// Todo: Move to Kit.
internal object Resources
{
	object MetaInf
	{
		private const val MetaInf = "META-INF"
		
		private val metaInfPath = Path(MetaInf)
		
		@JvmStatic
		fun open(path: Path) = Resources.open(metaInfPath / path)
		
		@JvmStatic
		fun open(path: String) = Resources.open("$MetaInf/$path")
	}
	
	@JvmStatic
	fun open(path: Path) = open(path, with = Resources::class.java)
	
	@JvmStatic
	fun open(path: String) = open(path, with = Resources::class.java)
	
	@JvmStatic
	private fun open(path: Path, with: Class<*>) = open("$path", with)
	
	@JvmStatic
	private fun open(path: String, with: Class<*>) =
		with.getResourceAsStream(path).let()
		{
			check(it != null) { "No resource was found at path $path." }
			
			it.source().buffer()
		}
}