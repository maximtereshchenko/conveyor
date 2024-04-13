import com.github.maximtereshchenko.conveyor.plugin.compile.CompilePlugin;

module com.github.maximtereshchenko.conveyor.plugin.compile {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.compiler;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with CompilePlugin;
}