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

import dev.strixpyrr.powerLoom.internal.toModId
import dev.strixpyrr.powerLoomTest.TextTest.modIdConversionShouldFail
import dev.strixpyrr.powerLoomTest.TextTest.modIdShouldBe
import io.kotest.assertions.throwables.shouldThrowUnit
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

object TextTest : StringSpec(
{
	"toModId produces expected output"()
	{
		// Transparent passthrough
		"test-mod" modIdShouldBe "test-mod"
		
		// Number prefix rejection
		"0test-mod".modIdConversionShouldFail()
		
		// Empty string rejection
		"".modIdConversionShouldFail()
		
		// Underscore or dash prefix skipping
		"_test-mod" modIdShouldBe "test-mod"
		"-test-mod" modIdShouldBe "test-mod"
		
		// Pascal case
		
		"TestMod" modIdShouldBe "test-mod"
		
		// Acronyms
		"XYZTestMod" modIdShouldBe "xyz-test-mod"
		
		// Numbers
		"TestMod9" modIdShouldBe "test-mod9"
		
		// Special character pass-over
		"%Test+Mod$" modIdShouldBe "test-mod"
		"TEST[Mod]"  modIdShouldBe "test-mod"
		
		// Underscore passthrough
		"Test_Mod" modIdShouldBe "test_mod"
		
		// Dash passthrough
		"Test-Mod" modIdShouldBe "test-mod"
		
		// Truncation
		"TestModThatHasAnIdThatIsTooLongForItsOwnGoodAndWillHaveToBeTruncatedOhGodWhyAmIDoingThisPleaseSendHelp"
			.modIdShouldBe("test-mod-that-has-an-id-that-is-too-long-for-its-own-good-and-wi")
		
		// Camel case
		
		"testMod" modIdShouldBe "test-mod"
	}
})
{
	@JvmStatic
	infix fun String.modIdShouldBe(expected: String)
	{
		print("Test case: $this -> ")
		
		val modId = toModId()
		
		println(modId)
		
		modId shouldBe expected
	}
	
	@JvmStatic
	fun String.modIdConversionShouldFail()
	{
		print("Test case: $this -> ")
		
		shouldThrowUnit<IllegalArgumentException>
		{
			val modId = toModId()
			
			println("$modId (didn't throw)")
		}
		
		println("(threw)")
	}
}