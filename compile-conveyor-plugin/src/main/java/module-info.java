import com.github.maximtereshchenko.conveyor.plugin.compile.CompilePlugin;

module com.github.maximtereshchenko.conveyor.plugin.compile {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.compiler;
    exports com.github.maximtereshchenko.conveyor.plugin.compile;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with CompilePlugin;
}