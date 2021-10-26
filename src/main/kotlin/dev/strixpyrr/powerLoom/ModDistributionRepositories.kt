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
package dev.strixpyrr.powerLoom

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

private const val   ModrinthUrl = "https://api.modrinth.com/maven"
private const val CurseMavenUrl = "https://cursemaven.com"

fun RepositoryHandler.modrinth() = maven(url = ModrinthUrl).run()
{
	name = "Modrinth"
	
	content { it.includeGroup(ModrinthMavenGroup) }
}

fun RepositoryHandler.curseMaven() = maven(url = CurseMavenUrl).run()
{
	name = "CurseMaven"
	
	content { it.includeGroup(CurseMavenGroup) }
}