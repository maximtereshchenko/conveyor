import com.github.maximtereshchenko.conveyor.plugin.clean.CleanPlugin;

module com.github.maximtereshchenko.conveyor.plugin.clean {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with CleanPlugin;
}