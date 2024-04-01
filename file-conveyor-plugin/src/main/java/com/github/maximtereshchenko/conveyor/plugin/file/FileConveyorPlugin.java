package com.github.maximtereshchenko.conveyor.plugin.file;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPluginConfiguration;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import java.util.Collection;
import java.util.List;

public final class FileConveyorPlugin implements ConveyorPlugin {

    @Override
    public Collection<ConveyorTaskBinding> bindings(ConveyorPluginConfiguration configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                new CreateFileTask(configuration.projectDirectory().resolve(configuration.value("name")))
            )
        );
    }
}
