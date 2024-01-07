package com.github.maximtereshchenko.conveyor.plugin.file;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPluginConfiguration;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTask;
import java.util.Collection;
import java.util.List;

public final class FileConveyorPlugin implements ConveyorPlugin {

    @Override
    public Collection<ConveyorTask> tasks(ConveyorPluginConfiguration configuration) {
        return List.of(new CreateFileTask(configuration.projectDirectory().resolve(configuration.value("name"))));
    }
}
