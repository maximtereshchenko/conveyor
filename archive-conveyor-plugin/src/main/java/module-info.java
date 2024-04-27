import com.github.maximtereshchenko.conveyor.plugin.archive.ArchivePlugin;

module com.github.maximtereshchenko.conveyor.plugin.archive {
    requires com.github.maximtereshchenko.conveyor.common.api;
    requires com.github.maximtereshchenko.conveyor.plugin.api;
    requires com.github.maximtereshchenko.zip;
    exports com.github.maximtereshchenko.conveyor.plugin.archive;
    provides com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin with ArchivePlugin;
}