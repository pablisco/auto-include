# Auto Include

[![Actions](https://github.com/pablisco/auto-include/workflows/Publish/badge.svg)](https://github.com/pablisco/auto-include/actions) 
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/pablisco/gradle/auto-include/plugin/maven-metadata.xml.svg?label=Gradle)](https://plugins.gradle.org/plugin/com.pablisco.gradle.auto.include)

A Gradle plugin that help you not have to include any modules manually in your settings file.

## Why do I need this?

If you have had any project with more than 10 modules and/or nested modules you've must have come across the problem of 
having to manage them inside your settings.gradle[.kts] file.

Well, Auto-Include does what you would expect, it looks up any potential modules you may have and adds them for you.

## Table of Contents 

<!-- toc -->
- __[How do I use it?](#how-do-i-use-it)__
- __[Ignore modules](#ignore-modules)__
- __[History](#history)__
- __[Local development](#local-development)__
- __[License](#license)__
<!-- /toc -->
 
## How do I use it?

Remove all your `include()` instructions inside `settings.gradle[.kts]` and add this:

```kotlin
plugins {
    id("com.pablisco.gradle.auto.include") version "1.1"
}
```

That's it!

Now, you may want to tweak your script a bit more with things like ignored modules.

## Include Composite Build Modules

The most common way to add logic or custom plugins to your build script is to use the buildSrc folder.

This is, in simple terms, a special project that is compiled before Gradle evaluates and runs the main 
build script. It's quite common to use that to define versions values, specially when using Gradle 
kotlin DSL as it provide auto-complete.

The problem with buildSrc is that it doesn't use cache, and it's compiled and tested (if you have tests) 
every time you run your build. You can read more about this on this article. A better alternative is to 
use composite builds.

They are also run before the main build script, with the added benefit of using build cache and allowing 
to have multiple modules for different build features you may have on your code base. If you look at the 
source code of this plugin, you will see that we have two different modules, inside the gradle folder that 
we use for dependencies and to check the version of the current version before publishing.

The standard procedure with composite builds is to create a "root" project anywhere on the code base and 
add it inside settings.gradle[.kts]:

```kotlin
includeBuild("gradle/dependencies")
```

Then you would have to create a plugin class and make sure it's applied in one of the modules, in order 
to access the code defined on that build modules. If you don't define and apply the plugin then the code 
will not be accessible.

With autoModule, things are a lot easier. If you create a build module inside the gradle folder, where 
you may have set up the gradle wrapper, then it'll get picked up and added to the build script. Quite 
similar to what happens with the normal modules.

On top of that, you no longer need to define a gradle plugin, which adds compile time on a clean build. 
The code will automatically be added to the classpath of the build script.

## Ignore modules

If you want to make sure a module *is not* included to the Gradle graph you can do it in two ways:

1. Adding the `.ignore` extension at the end of the `build.gradle[.kts]` script.
2. Inside `settings.gradle[.kts]` you can configure `auto.include` to do so:

```kotlin
auto.include {
  ignore(":modulePath", ":some:other:module")
}
```

## History 

This plugin comes straight out of [`Auto-Module`](https://github.com/pablisco/auto-module) which had this functionality
and much more. However, it probably has too much functionality, so I'm in the process of splitting some of it.
Some of this functionality (generating type safe modules) is also coming to Gradle as a feature.

## Local development

If you want to run this project locally there are a few things to consider:

- If you want to use the current version (the one you are editing) of the plugin, you will have to deploy it to the 
local `repo` with the `publish` gradle task. This is why all the `settings.gradle.kts`, including the ones for the local 
plugins, have code to include `./repo` as a plugin repository.

## License 

This project falls under the MIT License - see the [license.md](license.md) file for details
