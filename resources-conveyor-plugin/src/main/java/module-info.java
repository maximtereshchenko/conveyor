import com.github.maximtereshchenko.conveyor.plugin.resources.ResourcesPlugin;

module com.github.maximtereshchenko.conveyor.plugin.resources {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with ResourcesPlugin;
}