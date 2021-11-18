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

import com.github.ajalt.mordant.animation.ProgressAnimation
import com.github.ajalt.mordant.animation.progressAnimation
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.terminal.Terminal
import dev.strixpyrr.powerLoom.internal.distMap
import dev.strixpyrr.powerLoom.internal.writeTo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okio.Buffer
import okio.BufferedSource
import org.openapitools.client.infrastructure.ApiClient.Companion.client
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.coroutines.resumeWithException
import kotlin.io.path.*

internal object QueueDownloader
{
	@JvmStatic
	suspend fun downloadAsync(
		queue: Collection<Download>,
		overwrite: Boolean = false,
		limiter: Semaphore? = Semaphore(1)
	) = coroutineScope()
	{
		async(IO)
		{
			if (queue.isEmpty()) return@async Download.BatchResult.Empty
			
			val terminal = Terminal()
			
			terminal.info.updateTerminalSize()
			
			val tlProgress = terminal.progressAnimation()
			{
				completed()
				progressBar()
			}
			
			tlProgress.start()
			tlProgress.updateTotal(queue.size.toLong())
			
			suspend fun download(
				terminal: Terminal,
				download: Download,
				overwrite: Boolean,
				topLevelProgress: ProgressAnimation
			): Download.Result
			{
				val (source, target) = download
				
				val progressUpdater = ProgressUpdater(terminal)
				
				val dl = download(
					source.toHttpUrlOrNull()!!,
					to = Path(target),
					overwrite,
					progressUpdater
				)
				
				topLevelProgress.advance()
				
				return dl
			}
			
			val results =
				if (limiter == null)
					queue.distMap()
					{
						download(terminal, it, overwrite, tlProgress)
					}
				else
					queue.distMap(limiter)
					{
						download(terminal, it, overwrite, tlProgress)
					}
			
			tlProgress.stop()
			
			val successes = results.count { it.wasSuccessful }
			val failures = results.size - successes
			
			Download.BatchResult(
				successes,
				failures,
				results
			)
		}
	}
	
	@JvmStatic
	suspend fun download(
		source: HttpUrl,
		to: Path,
		overwrite: Boolean = false,
		progress: IProgressUpdater = NoOpUpdater
	): Download.Result
	{
		class ProgressSource(
			private val value: BufferedSource
		) : BufferedSource by value
		{
			// readAll calls read, so we can intercept data to update the progress.
			override fun read(sink: Buffer, byteCount: Long): Long
			{
				val count = value.read(sink, byteCount)
				
				if (count > -1L) progress.advance(count)
				
				return count
			}
		}
		
		val targetName = Path(source.encodedPath.decode()).name
		
		val target = if (to.isDirectory())
			to / targetName
		else to
		
		@Suppress("BlockingMethodInNonBlockingContext")
		target.parent.createDirectories()
		
		var code = -1
		
		progress.start(text = targetName)
		
		val success = if (!overwrite && target.exists())
		{
			progress.stop(text = cyan("$targetName: exists"))
			
			true
		}
		else get(source).use()
		{ response ->
			if (response.isSuccessful)
			{
				response.body.use()
				{ body ->
					if (body == null)
					{
						code = response.code
						
						progress.stop(
							text = red("$targetName: no response body; code was $code")
						)
						
						false
					}
					else try
					{
						progress.updateTotal(
							body.contentLength()
						)
						
						ProgressSource(body.source()).writeTo(target)
						
						code = response.code
						
						progress.stop(
							text = cyan("$targetName: done")
						)
						
						true
					}
					catch (e: IOException)
					{
						progress.stop(
							text = red("$targetName: IOException[${e.message}]")
						)
						
						false
					}
				}
			}
			else
			{
				code = response.code
				
				progress.stop(
					text = red("$targetName: code $code")
				)
				
				false
			}
		}
		
		return Download.Result(
			success, source.toUrl(), code, target
		)
	}
	
	// Http
	
	@JvmStatic
	internal suspend fun get(source: HttpUrl) = with(client)
	{
		coroutineScope()
		{
			val request =
				Request.Builder()
					.url(source)
					.get()
					.build()
			
			newCall(request).await()
		}
	}
	
	@JvmStatic
	private fun String.decode(): String =
		URLDecoder.decode(this, UTF_8).replace(' ', '+') // Decode interprets + as a space.
	
	// From: https://git.io/Jibm8, adapted to a newer version of Kotlin and remove
	// call stack tracing.
	@JvmStatic
	@OptIn(ExperimentalCoroutinesApi::class)
	internal suspend fun Call.await(): Response =
		suspendCancellableCoroutine()
		{ continuation ->
			enqueue(object : Callback
			{
				override fun onResponse(call: Call, response: Response)
				{
					continuation.resume(response) { }
				}
				
				override fun onFailure(call: Call, e: IOException)
				{
					if (continuation.isCancelled) return
					
					continuation.resumeWithException(e)
				}
			})
			
			continuation.invokeOnCancellation()
			{
				try
				{
					cancel()
				}
				catch (e: Throwable) { }
			}
		}
	
	// Progress
	
	sealed interface IProgressUpdater
	{
		fun start(text: String)
		
		fun update(completion: Long)
		fun updateTotal(total: Long)
		fun advance(amount: Long)
		
		fun stop(text: String?)
		
		companion object
		{
			// For testing.
			internal fun create(terminal: Terminal): IProgressUpdater =
				ProgressUpdater(terminal)
		}
	}
	
	private class ProgressUpdater(
		private val terminal: Terminal
	) : IProgressUpdater
	{
		private lateinit var animation: ProgressAnimation
		
		override fun start(text: String)
		{
			animation = terminal.progressAnimation()
			{
				text(text)
				percentage ()
				progressBar()
				completed  ()
				speed("B/s")
				timeRemaining()
			}
			
			animation.start()
			animation.updateTotal(null)
		}
		
		override fun update(completion: Long) = animation.update(completion)
		override fun updateTotal(total: Long) = animation.updateTotal(total)
		
		override fun advance(amount: Long) = animation.advance(amount)
		
		override fun stop(text: String?)
		{
			animation.stop()
			
			text?.let(terminal::print)
		}
	}
	
	private object NoOpUpdater : IProgressUpdater
	{
		override fun start(text: String) { }
		
		override fun update(completion: Long) { }
		override fun updateTotal(total: Long) { }
		override fun advance(amount: Long) { }
		
		override fun stop(text: String?) { }
	}
}