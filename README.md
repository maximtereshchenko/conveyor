# Conveyor

A build tool for Java projects

## Goal

The goal of this project IS NOT to compete with existing build tools, but rather have fun creating
it and learn specifics about building Java projects

## How to build

```shell
./gradlew build
```

After that, you can check out how to use the CLI

```shell
java -jar ./conveyor-cli/build/libs/conveyor-cli-1.0.0-standalone.jar --help
```

## Schematic definition

The schematic is defined in JSON format. For an example you can check
out [the schematic](conveyor.json) for the Conveyor itself. For a complete structure you can look at
[Java classes](conveyor-api/src/main/java/com/github/maximtereshchenko/conveyor/api/schematic/SchematicDefinition.java)
describing the schematic definition.

## Features

* Schematic
    * This is a definition of a project
    * Schematic is defined by an optional group, name and an optional version. If group or version
      is absent, then the schematic is defined by its template's group or version
    * A schematic can be constructed up to the specified stage: CLEAN, COMPILE, TEST, ARCHIVE or
      PUBLISH. Stages transition from COMPILE up to specified stage. CLEAN is a special stage, it
      should be explicitly specified to be active
    * Schematics are constructed in parallel by default
* Dependency version resolution
    * Given the same dependency is required but with different versions, the highest version wins
      taken into account the presence of the dependency requiring that version in the result class
      path
    * Version precedence is determined by the following rules:
        * Part of the version before the first dash is considered version components, after -
          qualifiers
        * Dot, dash and transition between characters and digits constitute a separator
        * When version components are equal, a version with qualifiers has lower precedence than a
          version without
        * Precedence for two versions is determined by comparing version components and then
          qualifiers from left to right until a difference is found as follows:
            * Identifiers consisting of digits are compared numerically
            * Identifiers with letters are compared lexically
            * Numeric identifiers always have lower precedence than non-numeric identifiers
            * A larger set of identifiers has a higher precedence than a smaller set, if all the
              preceding identifiers are equal
    * Preferences are defined in a schematic with a group, a name and a version
    * Preferences can be imported from a schematic by defining that schematic with a group, a name
      and a version as the inclusion in preferences. Given the same artifact preference is imported
      with different versions, then the highest version wins
    * Plugins are used with defined version. If plugin does not define its version, then version
      defined in preferences is used
    * Direct dependencies are used with defined version. If dependency does not define its version,
      then version defined in preferences is used
    * Transitive dependencies are used with versions defined in preferences. If preferences do not
      contain the dependency, the version defined in a schematic requiring this dependency is used
* Plugins
    * Plugins are defined in a schematic with a group, a name, an optional version and an optional
      configuration in a form of key-value pairs
    * Plugins are archived in a JAR and exported via provider-configuration
      file `META-INF/services/com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin`
      containing the name of the implementation class
    * Plugins are loaded via Java `ServiceLoader` mechanism from the class path containing required
      dependencies from the plugin's schematic
    * Given properties and the configuration from the schematic, plugin produces zero or more tasks
      bound to a stage and a step withing that stage
    * Configuration values can be interpolated with schematic properties using `${property.key}`
      syntax
    * A plugin can be disabled with a configuration key `enabled` equal to `false`
    * Plugins are inherited from a schematic used as a template
    * The version of the inherited plugin can be overridden in the schematic
    * The configuration value of the inherited plugin can be overridden in the schematic
    * The configuration value of the inherited plugin can be removed in the schematic by assigning
      empty string to the key
    * The configuration defined in the schematic is merged with the inherited configuration
* Tasks
    * Tasks bound to the same stage are executed in step ascending order (PREPARE, RUN, FINALIZE)
    * Tasks bound to the same stage and step are executed in order of originating plugins in the
      schematic definition
    * Given schematic dependencies, task performs operations on the project and optionally produces
      artifact to be used by other schematics
    * Task can opt in for caching by declaring its inputs and outputs
    * The property `conveyor.tasks.cache.directory` defines the directory, where tasks should store
      cached outputs. It is relative to the directory, where the schematic definition is located.
      The default value is `.conveyor-cache/tasks` located in the root schematic's directory
      following by directories consisting of schematic's group and name
* Properties
    * Properties are user-defined key-value pairs
    * Properties are inherited from a schematic used as a template
    * Inherited property can be overridden in a schematic
    * Inherited property can be removed in the schematic by assigning empty string to the key
    * The property `conveyor.schematic.group` can be used to interpolate the schematic's group. This
      property cannot be overridden
    * The property `conveyor.schematic.name` can be used to interpolate the schematic's name. This
      property cannot be overridden
    * The property `conveyor.schematic.version` can be used to interpolate the schematic's version.
      This property cannot be overridden
    * The property `conveyor.schematic.directory` can be used to interpolate the directory, in which
      the schematic is located. This property cannot be overridden
    * Properties, plugin group and version, dependency group and version and preference group and
      version can be interpolated with other properties using `${property.key}` syntax
* Dependencies
    * Dependencies are defined in a schematic with a group, a name, an optional version and an
      optional scope: IMPLEMENTATION (default) or TEST
    * Each dependency should come with a schematic
    * Dependencies are inherited from a schematic used as a template
    * Version and scope of the inherited dependency can be overridden
    * Schematic can define a dependency on other schematic with a group, a name and an optional
      scope
    * A transitive dependency can be excluded from a dependency by defining the dependency as the
      exclusion
* Inheritance
    * A schematic inherits properties, plugins, dependencies and repositories from another schematic
      used as a template. It is defined with a group, a name and a version. The
      property `conveyor.schematic.template.location` defines the path to the template. It is
      relative to the directory, where the schematic definition is located. It is not inherited from
      the template. The default value is `../conveyor.json`
    * A schematic has zero or more inclusions. It will be used as a template for included
      schematics. The path to the included schematic is relative to the directory, where the
      schematic definition is located. If the path points to a directory, then the `conveyor.json`
      file will be searched for in this directory
    * A schematic is constructed before its inclusions
    * A schematic is constructed after its schematic template
    * A schematic is constructed before other schematics, which require it as a dependency
    * A schematic is constructed up to the specified stage
    * Schematics are constructed in depth-first order
    * Schematics to be constructed depend on the initial targeted schematic. The targeted schematic
      will be constructed together with its schematic templates, inclusions and also with other
      schematics and their schematic templates, on which targeted schematic depends either directly
      or transitively
* Repositories
    * Repositories are defined in a schematic
    * Every repository has a name
    * Repositories are inherited from a schematic used as a template
    * Local directory can be used as a source of artifacts and schematics. It is defined by a path
      relative to the directory, where the schematic definition is located. The path can be
      overridden in a schematic
    * Remote repository is defined with a URL to a repository with `Maven 2` layout
    * The property `conveyor.repository.remote.cache.directory` defines the directory, where remote
      directories should store downloaded artifacts and schematics. It is relative to the directory,
      where the schematic definition is located. The default value is `.conveyor-cache/repository`
      located in the root schematic's directory
