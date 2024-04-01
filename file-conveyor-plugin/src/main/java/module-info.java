import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.file.FileConveyorPlugin;

module com.github.maximtereshchenko.conveyor.plugin.file {
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides ConveyorPlugin with FileConveyorPlugin;
}