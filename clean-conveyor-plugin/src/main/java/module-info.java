import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.clean.CleanConveyorPlugin;

module com.github.maximtereshchenko.conveyor.plugin.clean {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides ConveyorPlugin with CleanConveyorPlugin;
}