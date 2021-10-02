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
package dev.strixpyrr.powerLoom.metadata

internal fun <C : Collection<*>> C.nullIfEmpty() = ifEmpty { null }

internal fun <T> mutableList(capacity: Int): MutableList<T> = ArrayList(capacity)

internal fun <T> mutableList(capacity: Int, elements: List<T>) =
	mutableList<T>(capacity).apply { this += elements }

internal fun <T> List<T>?.toMutableListWithSpaceFor(capacity: Int) =
	if (isNullOrEmpty())
		mutableList(capacity)
	else mutableList(size + capacity, elements = this)

internal fun <K, V> mutableMap(capacity: Int): MutableMap<K, V> = LinkedHashMap(capacity)

internal fun <K, V> mutableMap(capacity: Int, elements: Map<K, V>) =
	mutableMap<K, V>(capacity).apply { this += elements }

internal fun <K, V> Map<K, V>?.toMutableMapWithSpaceFor(capacity: Int) =
	if (isNullOrEmpty())
		mutableMap(capacity)
	else mutableMap(size + capacity, elements = this)

internal infix fun Collection<*>?.sizeOr(default: Int) = this?.size ?: default

internal infix fun Map<*, *>?.sizeOr(default: Int) = this?.size ?: default
