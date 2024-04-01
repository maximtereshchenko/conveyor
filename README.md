# Conveyor

A build tool for Java projects

# Features

* Schematic
    * This is a definition of a project to package
* Manual
    * This is a definition of an already packaged artifact
* Module path
    * Given the same dependency is required but with different versions, the highest version wins taken into account the
      presence of the dependency requiring that version in the result module path
* Plugins
    * Plugins are defined in a manual or a schematic with a name, an optional version and an optional configuration in a
      form of key-value pairs
    * Plugins are packaged in a JAR and exported via Java module system
    * Plugins are loaded via Java module system from a module layer containing required dependencies from the plugin's
      manual
    * Given properties and the configuration from the schematic, plugin produces zero or more tasks bound to a stage and
      a step withing that stage
    * Configuration values can be templated with schematic properties using `${property.key}` syntax
    * Plugin can be disabled with a configuration key `enabled` equal to `false`
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
