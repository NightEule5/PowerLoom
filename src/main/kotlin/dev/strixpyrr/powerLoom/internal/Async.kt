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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

internal suspend inline fun <T, R> Collection<T>.distMap(
	crossinline transform: suspend (T) -> R
) = coroutineScope()
{
	map()
	{
		async { transform(it) }
	}.awaitAll()
}

internal suspend inline fun <T, R> Collection<T>.distMap(
	limiter: Semaphore,
	crossinline transform: suspend (T) -> R
) = coroutineScope()
{
	map()
	{
		async()
		{
			limiter.acquire()
			
			val r = transform(it)
			
			limiter.release()
			
			r
		}
	}.awaitAll()
}