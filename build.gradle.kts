
import dev.strixpyrr.shorthand.CompilerArgumentScope.Companion.RequiresOptIn
import dev.strixpyrr.shorthand.JvmDefaultMode.All
import dev.strixpyrr.shorthand.freeCompilerArgs

plugins {
	kotlin("jvm")                  version "1.5.31"
	kotlin("plugin.serialization") version "1.5.31"
	id("dev.strixpyrr.shorthand")
	`java-gradle-plugin`
}

group = "dev.strixpyrr.power-loom"
version = "0.0.1"

repositories()
{
	mavenCentral()
}

dependencies()
{
	implementation(kotlin("stdlib"))
	implementation(gradleApi())
	
	implementation(deps.okio)
	implementation(deps.kotlinx.serialization.json)
	
	testImplementation(deps.kotest)
}

kotlin()
{
	target.run()
	{
		compilations.forEach()
		{
			it.kotlinOptions.run()
			{
				jvmTarget       = "1.8"
				languageVersion = "1.5"
				
				freeCompilerArgs()
				{
					optIn(RequiresOptIn)
					
					jvmDefault = All
				}
			}
		}
	}
}

tasks()
{
	withType<Test>
	{
		useJUnitPlatform()
	}
}

gradlePlugin()
{
	plugins()
	{
		val powerLoom by creating()
		{
			id = "dev.strixpyrr.power-loom"
			implementationClass = "dev.strixpyrr.powerLoom.PowerLoomPlugin"
		}
	}
}