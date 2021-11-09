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
package dev.strixpyrr.powerLoom.internal

import okio.*
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import kotlin.io.use

// Todo: Move to kit-io.
internal fun BufferedSource.writeTo(
	path: Path,
	create: Boolean = true,
	replace: Boolean = true
) = use()
{ source ->
	path.sink(*toOpenOptionsArray(create, replace))
		.buffer()
		.use(source::readAll)
}

private fun toOpenOptionsArray(create: Boolean, replace: Boolean) =
	if (create)
		if (replace)
			arrayOf(CREATE, TRUNCATE_EXISTING)
		else arrayOf(CREATE)
	else
		if (replace)
			arrayOf(TRUNCATE_EXISTING)
		else emptyArray()

internal fun Path.hashSha256() = source().use { HashingSource.sha256(it).hash }
