import com.github.maximtereshchenko.conveyor.plugin.compile.CompilePlugin;

module com.github.maximtereshchenko.conveyor.plugin.compile {
    requires java.compiler;
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with CompilePlugin;
}