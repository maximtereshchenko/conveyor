import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.firstwithdependency.FileConveyorPlugin;

module com.github.maximtereshchenko.conveyor.plugin.firstwithdependency {
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.conveyor.plugin.dependency;
    provides ConveyorPlugin with FileConveyorPlugin;
}