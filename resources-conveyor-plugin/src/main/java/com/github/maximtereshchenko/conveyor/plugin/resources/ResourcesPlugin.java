package com.github.maximtereshchenko.conveyor.plugin.resources;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ResourcesPlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "resources-conveyor-plugin";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(
        ConveyorSchematic schematic,
        Map<String, String> configuration
    ) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.PREPARE,
                new CopyResourcesTask(
                    resourcesDirectory(schematic.discoveryDirectory(), "main"),
                    ProductType.EXPLODED_MODULE
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CopyResourcesTask(
                    resourcesDirectory(schematic.discoveryDirectory(), "test"),
                    ProductType.EXPLODED_TEST_MODULE
                )
            )
        );
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}
