module ${normalizedName} {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with ${normalizedName}.${normalizedName};
}
