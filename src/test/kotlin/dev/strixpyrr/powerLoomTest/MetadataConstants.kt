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

internal const val FabricModJson = "fabric.mod.json"

internal const val TestModId      = "test"
internal const val TestModVersion = "4.2.0"
internal const val TestIssuesLink = "https://test.mod/issues"
internal const val TestDiscord    = "https://discord.gg/testmod"
internal const val TestName       = "Test"
internal const val TestDesc       = "Very descriptive"

internal const val InvalidTestModId = "-test"

private const val      SvJsonField = """"schemaVersion":1"""
private const val      IdJsonField = """"id":"$TestModId""""
private const val VersionJsonField = """"version":"$TestModVersion""""
private const val ContactJsonField = """"contact":{"issues":"$TestIssuesLink","discord":"$TestDiscord"}"""
private const val    NameJsonField = """"name":"$TestName""""
private const val    DescJsonField = """"description":"$TestDesc""""
private const val DependsJsonField = """"depends":{"fabric-language-kotlin":[">= 1.6.5+kotlin.1.5.31"]}"""

private const val BaseJsonFragment       = "$IdJsonField,$VersionJsonField"
private const val BaseJsonFragmentWithSv = "$SvJsonField,$BaseJsonFragment"

internal const val BaseJson        = "{${BaseJsonFragment      }}"
internal const val BaseJsonWithSv  = "{${BaseJsonFragmentWithSv}}"
internal const val ContactJson     = "{${BaseJsonFragmentWithSv},$ContactJsonField}"
