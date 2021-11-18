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

// Todo: Move to kit-collections
internal infix fun <T> T.addedTo(collection: MutableCollection<in T>) =
	also(collection::add)

@OptIn(ExperimentalStdlibApi::class)
internal inline fun <T, Xk, Xv> Collection<T>.flatAssociateBy(
	allocation: Int,
	transform: (T) -> Map<Xk, Xv>
) = buildMap(size * allocation)
{
	for (value in this@flatAssociateBy)
		this += transform(value)
}
