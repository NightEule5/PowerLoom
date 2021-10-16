
import dev.strixpyrr.shorthand.CompilerArgumentScope.Companion.RequiresOptIn
import dev.strixpyrr.shorthand.JvmDefaultMode.All
import dev.strixpyrr.shorthand.creating
import dev.strixpyrr.shorthand.freeCompilerArgs
import dev.strixpyrr.shorthand.getting

plugins {
	kotlin("jvm")                  version "1.6.0-RC"
	kotlin("plugin.serialization") version "1.6.0-RC"
	id("dev.strixpyrr.shorthand")
	`maven-publish`
	`java-gradle-plugin`
}

group = "dev.strixpyrr.power-loom"
version = "0.1.0"

repositories()
{
	mavenCentral()
	maven(url = "https://maven.fabricmc.net")
}

dependencies()
{
	implementation(kotlin("stdlib"))
	implementation(kotlin("gradle-plugin"))
	implementation(gradleApi())
	implementation(gradleKotlinDsl())
	
	implementation(deps.okio)
	implementation(deps.kotlinx.serialization.json)
	implementation(deps.fabric.loom)
	
	testImplementation(deps.kotest)
	testImplementation(gradleTestKit())
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
	
	val testClasses by getting()
	{
		// Why tf is this not done by default? O_o
		dependsOn("pluginUnderTestMetadata")
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

publishing()
{
	publications()
	{
		val powerLoom: MavenPublication by creating()
		{
			from(components["kotlin"])
			
			artifact(tasks.kotlinSourcesJar)
		}
	}
}