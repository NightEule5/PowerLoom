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

import dev.strixpyrr.powerLoom.metadata.*
import dev.strixpyrr.powerLoom.metadata.FabricMod.Licenses.Apache
import dev.strixpyrr.powerLoomTest.FabricModMetadataTest.testContactInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okio.Buffer
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.io.path.Path

@Suppress("BlockingMethodInNonBlockingContext")
object FabricModMetadataTest : StringSpec(
{
	"Rejects invalid Id"()
	{
		shouldThrow<IllegalArgumentException>
		{
			FabricMod(
				id      = InvalidTestModId,
				version = TestModVersion
			)
		}
	}
	
	// Tests omission of empty collections.
	"Generates bare-minimum fabric.mod.json"()
	{
		val buffer = Buffer()
		
		FabricMod(
			id      = TestModId,
			version = TestModVersion
		).encode(buffer)
		
		buffer.readUtf8() shouldBe BaseJsonWithSv
	}
	
	"Generates additional contact info correctly"()
	{
		val buffer = Buffer()
		
		FabricMod(
			id      = TestModId,
			version = TestModVersion,
			contact = testContactInfo
		).encode(buffer)
		
		buffer.readUtf8() shouldBe ContactJson
	}
	
	"Parses additional contact info correctly"()
	{
		val buffer = Buffer()
		
		buffer.writeUtf8(ContactJson)
		
		val value = FabricMod.decode(buffer)
		
		value.id      shouldBe TestModId
		value.version shouldBe TestModVersion
		value.contact shouldBe testContactInfo
	}
	
	"MutableFabricMod serializes via Java"()
	{
		@Suppress("SpellCheckingInspection")
		val data = MutableFabricMod(
			id = TestModId,
			version = TestModVersion,
			jars = mutableListOf(
				NestedJar("META-INF/jars/other.jar")
			),
			mixins = mutableListOf(
				Mixin("$TestModId.mixins.json")
			),
			languageAdapters = mutableMapOf(
				"non-default-language" to "*"
			),
			accessWidener = Path("$TestModId.accesswidener"),
		)
		
		data()
		{
			entryPoints()
			{
				kotlinCommon(TestEntryPointPackage, TestEntryPointFile, "commonInit")
				kotlinServer(TestEntryPointPackage, TestEntryPointFile, "serverInit")
				kotlinClient(TestEntryPointPackage, TestEntryPointFile, "clientInit")
			}
			
			dependencies()
			{
				dependOn       ("dependency",     "*")
				recommend      ("recommendation", "*")
				suggest        ("suggestion",     "*")
				specifyConflict("conflict",       "*")
				specifyBreak   ("break",          "*")
			}
			
			metadata()
			{
				name = TestName
				description = TestDesc
				
				author("TestAuthor")
				
				contributor("TestContributor")
				
				license = Apache
				
				icon = Icon("icon.png")
			}
		}
		
		val buffer = Buffer()
		
		ObjectOutputStream(
			buffer.outputStream()
		).use { it.writeObject(data) }
		
		ObjectInputStream(
			buffer.inputStream()
		).use { it.readObject() }
	}
})
{
	@JvmField
	internal val testContactInfo = ContactInfo(
		issues = TestIssuesLink,
		additional = mapOf("discord" to TestDiscord)
	)
}