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
@file:Suppress("NOTHING_TO_INLINE")

package dev.strixpyrr.powerLoom.internal

import dev.strixpyrr.powerLoom.metadata.FabricMod.Companion.IdRegex
import kotlin.math.min

/**
 * Converts an input project name string into a valid mod Id. The input cannot be
 * empty, and must not start with a digit.
 */
internal fun String.toModId(): String
{
	if (this matches IdRegex) return this
	
	require(isNotEmpty()        ) { "The input cannot be empty."           }
	require(this[0] !in '0'..'9') { "The input cannot start with a digit." }
	
	val scratch = StringBuilder(64)
	
	var offset = 0
	
	for ((i, c) in withIndex())
		when (i)
		{
			0    -> when (c)
			{
				in 'A'..'Z' ->
				{
					scratch += c.lowercaseChar()
					
					offset = 1
				}
				in 'a'..'z' -> { }
				else        -> offset = 1 // Skip preceding _, -, or foreign chars
			}
			else ->
			{
				if (scratch.length >= 64) break
				
				when (c)
				{
					in 'A'..'Z'  ->
					{
						scratch.append(this, offset, i)
						
						when (this[i - 1])
						{
							'-', '_'     -> scratch
							in 'A'..'Z'  ->
							{
								// Check for acronyms
								for (j in (i + 1) until length)
								{
									val next = this[j]
									
									when
									{
										 next !in 'A'..'Z' &&
										(next  in 'a'..'z' ||
										 next  in '0'..'9')   ->
										{
											scratch += '-'
											
											break
										}
										else -> break
									}
								}
								
								scratch
							}
							in 'a'..'z',
							in '0'..'9'  -> scratch.append('-')
							else         ->
							{
								// The current uppercase character has a preceding
								// foreign char. If the foreign char isn't the first,
								// repeat the current lookbehind until a known case
								// is reached.
								
								if (i > 1)
								{
									// Todo: Replace this with a tailrec function.
									
									var j = i - 2
									
									while (j-- > 0)
										when (this[j])
										{
											in 'A'..'Z', '-', '_'    ->
												if (i < length && this[j + 1] in 'A'..'Z')
												{
													scratch += '-'
													
													break
												}
											in 'a'..'z', in '0'..'9' ->
											{
												scratch += '-'
												
												break
											}
										}
								}
								
								scratch
							}
						} += c.lowercaseChar()
						
						offset = i + 1
					}
					in 'a'..'z',
					in '0'..'9',
					'-', '_'     -> { }
					else         ->
					{
						scratch.append(this, offset, i)
						
						offset = i + 1 // Skip foreign characters
					}
				}
			}
		}
	
	// I forgot this line before and spent a lot of time debugging. XD
	scratch.append(this, offset, length)
	return scratch.substring(0, min(64, scratch.length))
}

private inline operator fun StringBuilder.plusAssign(value: Char  ) { append(value) }
private inline operator fun StringBuilder.plusAssign(value: String) { append(value) }