package com.github.maximtereshchenko.conveyor.plugin.clean;

import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorProject;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class CleanConveyorPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "clean-conveyor-plugin";
    }

    @Override
    public Collection<ConveyorTaskBinding> bindings(ConveyorProject project, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.CLEAN,
                Step.RUN,
                new CleanConveyorTask(project.buildDirectory())
            )
        );
    }
}
