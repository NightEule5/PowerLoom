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

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

private const val GenerateModMetadataName = "generateModMetadata"
internal const val WriteModConfigsName = "writeModConfigs"

@Suppress("UnstableApiUsage")
internal fun KotlinSourceSet.toGenerateModMetadataName() =
	if (name == "main")
		GenerateModMetadataName
	else "generate${name}ModMetadata"

internal const val TaskGroup = "power loom"