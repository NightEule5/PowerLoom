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

internal typealias FacetElement = Pair<FacetKey, String>

internal typealias NestedMutableList<T> = MutableList<MutableList<T>>

internal fun facets(facet: FacetElement) = facets { single(facet) }

internal inline fun facets(block: FacetContainer.() -> Unit) =
	FacetContainer().apply(block).toList()

// I admit, this is a bit overkill. But at least it looks nice! :D
internal class FacetContainer
{
	private val andExpression = And()
	
	fun single(facet: FacetElement) = andExpression and facet
	
	fun single(or: Or) = andExpression and or
	
	infix fun FacetElement.and(other: FacetElement) =
		andExpression and this and other
	
	infix fun FacetElement.and(other: Or) =
		andExpression and this and other
	
	infix fun FacetElement.or(other: FacetElement) =
		Or().also { andExpression and (it or this or other) }
	
	@JvmInline
	value class And(
		val orExpressions: NestedMutableList<String> = mutableListOf()
	)
	{
		infix fun and(other: FacetElement) = and(Or(other))
		
		infix fun and(other: Or) = apply { orExpressions += other.facets }
	}
	
	@JvmInline
	value class Or(
		val facets: MutableList<String> = mutableListOf()
	)
	{
		constructor(
			facet: FacetElement
		) : this(
			mutableListOf(facet.format())
		)
		
		infix fun or(other: FacetElement) = apply { facets += other.format() }
	}
	
	fun toList() = andExpression.orExpressions
}

internal enum class FacetKey
{
	Categories,
	Versions,
	License,
	ProjectType
}

internal fun FacetElement.format(): String
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