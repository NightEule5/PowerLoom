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

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSource

interface IHashable
{
	fun hash(): ByteString
	
	companion object
	{
		@JvmStatic fun String.hash() = encodeUtf8().sha256()
		
		@JvmStatic fun BufferedSource.hash() = HashingSource.sha256(this).hash
		
		@JvmStatic fun Buffer.hash() = sha256()
	}
}
