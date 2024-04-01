import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.secondwithdependency.FileConveyorPlugin;

module com.github.maximtereshchenko.conveyor.plugin.secondwithdependency {
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.plugin.dependency;
    provides ConveyorPlugin with FileConveyorPlugin;
}