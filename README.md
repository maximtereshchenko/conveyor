# Conveyor

A build tool for Java projects

# Features

* Schematic
    * This is a definition of a project to construct
    * A schematic can be constructed up to the specified stage: CLEAN, COMPILE, TEST, ARCHIVE or PUBLISH
* Manual
    * This is a definition of an already archived artifact
    * It is a subset of schematic
* Dependency version resolution
    * Given the same dependency is required but with different versions, the highest version wins taken into account the
      presence of the dependency requiring that version in the result module path
    * Preferences are defined in a manual or a schematic with a name and a version
    * Plugins are used with defined version. If plugin does not define its version, then version defined in preferences
      is used
    * Direct dependencies are used with defined version. If dependency does not define its version, then version defined
      in preferences is used
    * Transitive dependencies are used with versions defined in preferences. If preferences do not contain the
      dependency, the version defined in a manual requiring this dependency is used
    * Preferences can be imported from a manual by defining that manual with a name and a version as the inclusion in
      preferences
* Plugins
    * Plugins are defined in a manual or a schematic with a name, an optional version and an optional configuration in a
      form of key-value pairs
    * Plugins are archived in a JAR and exported via Java module system
    * Plugins are loaded via Java module system from a module layer containing required dependencies from the plugin's
      manual
    * Given properties and the configuration from the schematic, plugin produces zero or more tasks bound to a stage and
      a step withing that stage
    * Configuration values can be interpolated with schematic properties using `${property.key}` syntax
    * A plugin can be disabled with a configuration key `enabled` equal to `false`
    * Plugins are inherited from a manual or a schematic used as a template
    * The version of the inherited plugin can be overridden in the schematic
    * The configuration value of the inherited plugin can be overridden in the schematic
    * The configuration value of the inherited plugin can be removed in the schematic by assigning empty string to the
      key
    * The configuration defined in the schematic is merged with the inherited configuration
* Tasks
    * Only tasks bound to the stage equal to or lower the target stage will be executed during the construction of the
      schematic
    * Tasks are executed in stage ascending order (CLEAN, COMPILE, TEST, ARCHIVE, PUBLISH)
    * Tasks bound to the same stage are executed in step ascending order (PREPARE, RUN, FINALIZE)
    * Given schematic dependencies and products, task performs operations on the project and produces products to be
      used in subsequent tasks
* Properties
    * Properties are user-defined key-value pairs
    * Properties are inherited from a manual or a schematic used as a template
    * Inherited property can be overridden in a schematic
    * Inherited property can be removed in the schematic by assigning empty string to the key
    * The property `conveyor.schematic.name` can be used to interpolate the schematic's name. This property cannot be
      overridden
    * The property `conveyor.schematic.version` can be used to interpolate the schematic's version. This property cannot
      be overridden
    * The property `conveyor.discovery.directory` defines the directory, where plugins should find files to work with.
      It is relative to the directory, where the schematic definition is located
    * The property `conveyor.construction.directory` defines the directory, where plugins should place created products.
      It is relative to the directory, where the schematic definition is located
    * Properties, plugin versions, dependency versions and preference versions can be interpolated with other properties
      using `${property.key}` syntax
* Dependencies
    * Dependencies are defined in a manual or a schematic with a name, an optional version and an optional scope:
      IMPLEMENTATION (default) or TEST
    * Each dependency should come with a manual
    * Dependencies are inherited from a manual or a schematic used as a template
    * Version and scope of the inherited dependency can be overridden
    * Schematic can define a dependency on other schematic with a name and an optional scope. In such case the product
      from this schematic of type MODULE will be used in module path
* Inheritance
    * A schematic inherits properties, plugins and dependencies from a manual used as a template. It is defined with a
      name and a version
    * A schematic inherits properties, plugins, dependencies and repositories from another schematic used as a template.
      It is defined with a path to the file with the schematic
    * If a schematic does not have an explicitly defined template, then a schematic from `conveyor.json` file located in
      the parent directory will be used. If such schematic does not exist, then manual with `super-manual` name and
      version `1` will be used
    * A schematic has zero or more inclusions. It will be used as a template for included schematics
    * A schematic is constructed before its inclusions
    * A schematic is constructed after its schematic template
    * A schematic is constructed before other schematics, which require it as a dependency
    * A schematic is constructed up to the specified stage or ARCHIVE, whichever is higher, if it is required as a
      dependency for other schematic
    * Schematics are constructed in depth-first order
    * Schematics to be constructed depend on the initial targeted schematic. The targeted schematic will be constructed
      together with its schematic templates, inclusions and also with other schematics and their schematic templates, on
      which targeted schematic depends either directly or transitively
* Repositories
    * Repositories are defined in a schematic
    * Every repository has a name and an optional `enabled` flag
    * A repository can be disabled with the `enabled` flag equal to `false`
    * Repositories are inherited from a schematic used as a template
    * Local directory can be used as a source of artifacts and manuals. It is defined by a path relative to the
      directory, where the schematic definition is located. The path can be overridden in a schematic
    * Remote repository is defined with a URL to a repository with Maven2 layout. 
