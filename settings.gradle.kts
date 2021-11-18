enableFeaturePreview("VERSION_CATALOGS")

pluginManagement()
{
	repositories()
	{
		gradlePluginPortal()
		maven(url = "https://jitpack.io")
	}
	
	plugins()
	{
		id("dev.strixpyrr.shorthand") version "0.0.5"
	}
}

dependencyResolutionManagement()
{
	versionCatalogs()
	{
		val deps by creating()
		{
			from(files("versions.toml"))
		}
	}
}

rootProject.name = "powerloom"
