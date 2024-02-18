# Conveyor

A build tool for Java projects

# Features

* Schematic - definition of a project to package
* Manual - definition of an already packaged artifact
* Plugins
    * Plugins are defined in a manual or a schematic with a name, an optional version and an optional configuration in a
      form of key-value pairs
    * Plugins are packaged in a JAR and exported via Java module system
    * Plugins are loaded via Java module system from a module layer containing required dependencies from the plugin's
      manual.
      `Note: given plugins require the same dependency, the highest version wins taken into account the presence of the dependency requiring that version in the result module path`
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
