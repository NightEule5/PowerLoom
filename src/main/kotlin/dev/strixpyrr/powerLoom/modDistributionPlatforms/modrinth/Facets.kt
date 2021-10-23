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
package dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth

import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetKey.*

// I admit, this is a bit overkill. But at least it looks nice! :D
@JvmInline
internal value class FacetSet(
	val list: MutableList<MutableList<String>> = mutableListOf()
)
{
	private fun single(facet: Pair<FacetKey, String>) =
		Or(
			mutableListOf<String>().also { list += it }
		)
	
	infix fun Pair<FacetKey, String>.or(facet: Pair<FacetKey, String>) = single(facet) or facet
	
	infix fun Pair<FacetKey, String>.and(facet: Pair<FacetKey, String>) =
		And(list) and this and facet
	
	infix fun Or.or(facet: Pair<FacetKey, String>) = apply()
	{
		value += facet.format()
	}
	
	infix fun Or.and(facet: Pair<FacetKey, String>) = this and single(facet)
	
	infix fun Or.and(facet: Or) = And(list)
	
	infix fun And.and(facet: Pair<FacetKey, String>) = this and single(facet)
	
	infix fun And.and(facet: Or) = this
	
	@JvmInline
	value class Or(
		val value: MutableList<String>
	)
	
	@JvmInline
	value class And(
		private val list: MutableList<MutableList<String>>
	)
	
	companion object
	{
		inline operator fun invoke(block: FacetSet.() -> Unit) = FacetSet().apply(block)
	}
}

internal enum class FacetKey
{
	Categories,
	Versions,
	License,
	ProjectType
}

internal fun Pair<FacetKey, String>.format(): String
{
	val (key, value) = this
	
	return "${key.format()}:$value"
}

private fun FacetKey.format() = when (this)
{
	Categories  -> "categories"
	Versions    -> "versions"
	License     -> "license"
	ProjectType -> "project_type"
}