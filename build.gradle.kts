@file:Suppress("UnstableApiUsage")

import dev.strixpyrr.shorthand.*
import dev.strixpyrr.shorthand.CompilerArgumentScope.Companion.RequiresOptIn
import dev.strixpyrr.shorthand.JvmDefaultMode.All
import de.undercouch.gradle.tasks.download.Download as Download

plugins {
	kotlin("jvm")                  version "1.6.0-RC"
	kotlin("plugin.serialization") version "1.6.0-RC"
	alias(deps.plugins.shorthand)
	alias(deps.plugins.openapi  )
	alias(deps.plugins.download )
	`maven-publish`
	`java-gradle-plugin`
}

group = "dev.strixpyrr.powerloom"
version = "0.1.1.2"

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
	
	implementation(deps.kotlinx.serialization.json)
	implementation(deps.kotlinx.couroutines.core)
	implementation(deps.fabric.loom)
	implementation(deps.square.okio)
	implementation(deps.square.retrofit)
	implementation(deps.square.retrofit.scalars)
	implementation(deps.square.okhttp3.logging)
	implementation(deps.retrofitSerializationConverter)
	
	testImplementation(deps.kotest)
	testImplementation(gradleTestKit())
}

val openApiOutPath = "$buildDir/generated-sources/openapi"

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
	
	sourceSets()
	{
		main()
		{
			kotlin.run()
			{
				srcDir("$openApiOutPath/src/main/kotlin")
			}
		}
	}
}

val openApiSpecPath = "$buildDir/openapi-specs/modrinth.yaml"

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
	
	val downloadModrinthOpenApiSpec: Download by creating()
	{
		src("https://docs.modrinth.com/openapi.yaml")
		
		dest(openApiSpecPath)
		
		overwrite(false)
	}
	
	val openApiGenerate by getting()
	{
		dependsOn(downloadModrinthOpenApiSpec)
	}
}

openApiGenerate()
{
	generatorName("kotlin")
	
	inputSpec(openApiSpecPath)
	outputDir(openApiOutPath)
	
	ignoreFileOverride("$rootDir/.openapi-generator-ignore")
	
	// Retrofit generation is broken, we'll just use the default for now.
	// library("jvm-retrofit2")
	
	val basePackage = "dev.strixpyrr.powerLoom.modDistributionPlatforms.modrinth"
	
		apiPackage(basePackage)
	invokerPackage(basePackage)
	  modelPackage(basePackage)
	modelNamePrefix("Modrinth")
	
	configOptions += mapOf(
		"enumPropertyNaming"   to "PascalCase",
		"useCoroutines"        to "true",
		"serializationLibrary" to "kotlinx_serialization",
		"apiSuffix"            to "Service"
	)
}

gradlePlugin()
{
	plugins()
	{
		val powerLoom by creating()
		{
			id = "dev.strixpyrr.powerloom"
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