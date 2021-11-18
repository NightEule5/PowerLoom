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

import com.github.ajalt.mordant.terminal.Terminal
import dev.strixpyrr.powerLoom.tasks.QueueDownloader
import dev.strixpyrr.powerLoom.tasks.QueueDownloader.IProgressUpdater
import dev.strixpyrr.powerLoomTest.QueueDownloaderTest.RootPath
import dev.strixpyrr.powerLoomTest.QueueDownloaderTest.SodiumJar
import dev.strixpyrr.powerLoomTest.QueueDownloaderTest.SodiumUrl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.div
import kotlin.io.path.name

@Suppress("BlockingMethodInNonBlockingContext")
object QueueDownloaderTest : StringSpec(
{
	"Single download"()
	{
		val directory = Path("$RootPath/singleNonOverwriting")
		
		(directory / SodiumJar).deleteExisting()
		
		val (wasSuccessful, source, code, target) = QueueDownloader.download(
			SodiumUrl.toHttpUrl(),
			directory,
			progress = IProgressUpdater.create(Terminal())
		)
		
		wasSuccessful.shouldBeTrue()
		"$source" shouldBe SodiumUrl
		code      shouldBe 200
		target.shouldExist()
		target.name shouldBe SodiumJar
	}
})
{
	private const val RootPath = "run/downloader"
	
	private const val SodiumJar =
		"sodium-fabric-mc1.17.1-0.3.2+build.7.jar"
	
	private const val SodiumUrl =
		"https://cdn.modrinth.com/data/AANobbMI/versions/mc1.17.1-0.3.2/$SodiumJar"
}