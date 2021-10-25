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

import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetSet
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.format
import dev.strixpyrr.powerLoomTest.ModrinthTest.Category
import dev.strixpyrr.powerLoomTest.ModrinthTest.CategoryFacet
import dev.strixpyrr.powerLoomTest.ModrinthTest.License
import dev.strixpyrr.powerLoomTest.ModrinthTest.LicenseFacet
import dev.strixpyrr.powerLoomTest.ModrinthTest.ProjType
import dev.strixpyrr.powerLoomTest.ModrinthTest.ProjTypeFacet
import dev.strixpyrr.powerLoomTest.ModrinthTest.Version1
import dev.strixpyrr.powerLoomTest.ModrinthTest.Version2
import dev.strixpyrr.powerLoomTest.ModrinthTest.VersionsFacet1
import dev.strixpyrr.powerLoomTest.ModrinthTest.VersionsFacet2
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetKey.Categories as CategoriesKey
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetKey.License as LicenseKey
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetKey.ProjectType as ProjectTypeKey
import dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth.FacetKey.Versions as VersionsKey

object ModrinthTest : StringSpec(
{
	val version1 =    VersionsKey to Version1
	val version2 =    VersionsKey to Version2
	val projType = ProjectTypeKey to ProjType
	val license  =     LicenseKey to License
	val category =  CategoriesKey to Category
	
	"Facets: pairs format properly"()
	{
		version1.format() shouldBe VersionsFacet1
		version2.format() shouldBe VersionsFacet2
		projType.format() shouldBe ProjTypeFacet
		license .format() shouldBe  LicenseFacet
		category.format() shouldBe CategoryFacet
	}
	
	"Facets: \"and\" translates to top level"()
	{
		val facets = FacetSet()
		{
			version1 and projType and license
		}.list
		
		facets shouldHaveSize 3
		
		val (versionsStr, projTypeStr, licenseStr) = facets
		
		versionsStr shouldHaveSize 1
		projTypeStr shouldHaveSize 1
		 licenseStr shouldHaveSize 1
	}
	
	"Facets: \"or\" translates to nested"()
	{
		val facets = FacetSet()
		{
			version1 or version2
		}.list
		
		facets shouldHaveSize 1
		
		val orExpression = facets[0]
		
		orExpression shouldHaveSize 2
		
		orExpression[0] shouldBe VersionsFacet1
		orExpression[1] shouldBe VersionsFacet2
	}
})
{
	private const val Version1 = "1.16.5"
	private const val Version2 = "1.17.1"
	private const val ProjType = "mod"
	private const val License  = "Apache-2.0"
	private const val Category = "fabric"
	
	private const val VersionsFacet1 = "versions:$Version1"
	private const val VersionsFacet2 = "versions:$Version2"
	private const val ProjTypeFacet  = "project_type:$ProjType"
	private const val  LicenseFacet  = "license:$License"
	private const val CategoryFacet  = "categories:$Category"
}