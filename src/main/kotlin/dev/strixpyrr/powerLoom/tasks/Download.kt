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

import java.io.Serializable
import java.net.URL
import java.nio.file.Path

data class Download(
	val source: URL,
	val target: String
) : Serializable
{
	constructor(source: URL   , target: Path  ) : this(source, "$target")
	constructor(source: String, target: Path  ) : this(URL(source), target)
	constructor(source: String, target: String) : this(URL(source), target)
	
	data class Result(
		val wasSuccessful: Boolean,
		val source: URL,
		val code  : Int,
		val target: Path
	)
	
	data class BatchResult(
		val successfulCount: Int,
		val     failedCount: Int,
		val files: List<Result>
	)
	{
		companion object
		{
			@JvmField
			val Empty = BatchResult(
				successfulCount = 0,
				failedCount = 0,
				files = emptyList()
			)
		}
	}
	
	companion object
	{
		private const val serialVersionUID = 1L
	}
}
