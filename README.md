ProPatcher
==========

ProPatcher is a Gradle plugin for creating patch files on the go.

## Installation

ProPatcher has been added to Gradle's plugin portal, and can be used using the new
plugin mechanism introduced in Gradle 2.1.
You can find the plugin [here](https://plugins.gradle.org/plugin/uk.jamierocks.propatcher).

```gradle
plugins {
    id 'uk.jamierocks.propatcher' version '2.0.0'
}
```

For those of you, using builds where you cannot utilise the new plugin mechanism,
or are using a version of Gradle prior to 2.1, here is the old example:

```gradle
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
    }
    dependencies {
        classpath 'gradle.plugin.uk.jamierocks:propatcher:2.0.0'
    }
}

apply plugin: 'uk.jamierocks.propatcher'
```

## Example

```gradle
patches {
    // This is a directory input, you can also use zip file inputs using rootZip
    rootDir = file('root')
    // This is a directory input
    target = file('target')
    
    // This is a directory output
    patches = file('patches')
}
```

## Tasks

| Name           | Description                                           |
| -------------- | ----------------------------------------------------- |
| `makePatches`  | Make all necessary patch files.                       |
| `applyPatches` | Apply all patches to the target.                      |
| `resetSources` | Resets the target, to it's original unmodified state. |
