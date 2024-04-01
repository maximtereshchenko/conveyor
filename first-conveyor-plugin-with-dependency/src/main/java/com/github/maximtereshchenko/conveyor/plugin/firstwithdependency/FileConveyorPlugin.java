package com.github.maximtereshchenko.conveyor.plugin.firstwithdependency;

import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPluginConfiguration;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import com.github.maximtereshchenko.conveyor.plugin.api.Stage;
import com.github.maximtereshchenko.conveyor.plugin.dependency.Dependency;
import java.util.Collection;
import java.util.List;

public final class FileConveyorPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "first-conveyor-plugin-with-dependency";
    }

    @Override
    public Collection<ConveyorTaskBinding> bindings(ConveyorPluginConfiguration configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                new CreateFileTask(
                    configuration.projectDirectory()
                        .resolve("first-" + new Dependency().version())
                )
            )
        );
    }
}
