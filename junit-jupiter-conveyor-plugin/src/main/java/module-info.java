import com.github.maximtereshchenko.conveyor.plugin.junit.jupiter.JunitJupiterPlugin;

module com.github.maximtereshchenko.conveyor.plugin.junit.jupiter {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires org.junit.platform.launcher;
    uses org.junit.platform.engine.TestEngine;
    exports com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with JunitJupiterPlugin;
}