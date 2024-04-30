# Conveyor

A build tool for Java projects

# Features

* Schematic
    * This is a definition of a project
    * A schematic can be constructed up to the specified stage: CLEAN, COMPILE, TEST, ARCHIVE or
      PUBLISH
* Dependency version resolution
    * Given the same dependency is required but with different versions, the highest version wins
      taken into account the presence of the dependency requiring that version in the result class
      path
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
    * Only tasks bound to the stage equal to or lower the target stage will be executed during the
      construction of the schematic
    * Tasks are executed in stage ascending order (CLEAN, COMPILE, TEST, ARCHIVE, PUBLISH)
    * Tasks bound to the same stage are executed in step ascending order (PREPARE, RUN, FINALIZE)
    * Tasks bound to the same stage and step are executed in order of originating plugins in the
      schematic definition
    * Given schematic dependencies and products, task performs operations on the project and
      produces products to be used in subsequent tasks
* Properties
    * Properties are user-defined key-value pairs
    * Properties are inherited from a schematic used as a template
    * Inherited property can be overridden in a schematic
    * Inherited property can be removed in the schematic by assigning empty string to the key
    * The property `conveyor.schematic.name` can be used to interpolate the schematic's name. This
      property cannot be overridden
    * The property `conveyor.schematic.version` can be used to interpolate the schematic's version.
      This property cannot be overridden
    * The property `conveyor.discovery.directory` defines the directory, where plugins should find
      files to work with. It is relative to the directory, where the schematic definition is
      located. The default value is `./`
    * The property `conveyor.construction.directory` defines the directory, where plugins should
      place created products. It is relative to the directory, where the schematic definition is
      located. The default value is `./.conveyor`
    * Properties, plugin group and version, dependency group and version and preference group and
      version can be interpolated with other properties using `${property.key}` syntax
* Dependencies
    * Dependencies are defined in a schematic with a group, a name, an optional version and an
      optional scope: IMPLEMENTATION (default) or TEST
    * Each dependency should come with a schematic
    * Dependencies are inherited from a schematic used as a template
    * Version and scope of the inherited dependency can be overridden
    * Schematic can define a dependency on other schematic with a group, a name and an optional
      scope. In such case the product from this schematic of type `JAR` or `EXPLODED_JAR` (if `JAR`
      is absent) will be used in class path
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
      schematic definition is located
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
    * Every repository has a name and an optional `enabled` flag
    * A repository can be disabled with the `enabled` flag equal to `false`
    * Repositories are inherited from a schematic used as a template
    * Local directory can be used as a source of artifacts and schematics. It is defined by a path
      relative to the directory, where the schematic definition is located. The path can be
      overridden in a schematic
    * Remote repository is defined with a URL to a repository with `Maven 2` layout
    * The property `conveyor.repository.remote.cache.directory` defines the directory, where remote
      directories should store downloaded artifacts and schematics. It is relative to the directory,
      where the schematic definition is located. The default value is `.conveyor-cache` located in
      the root schematic's directory
