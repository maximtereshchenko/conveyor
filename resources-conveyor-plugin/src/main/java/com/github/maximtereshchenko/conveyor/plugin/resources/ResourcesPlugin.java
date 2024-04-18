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
        var mainResources = resourcesDirectory(schematic.discoveryDirectory(), "main");
        var testResources = resourcesDirectory(schematic.discoveryDirectory(), "test");
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.PREPARE,
                new DiscoverResourcesTask(
                    mainResources,
                    ProductType.RESOURCE,
                    schematic.coordinates()
                )
            ),
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.FINALIZE,
                new CopyResourcesTask(
                    ProductType.EXPLODED_MODULE,
                    ProductType.RESOURCE,
                    schematic.coordinates(),
                    mainResources
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new DiscoverResourcesTask(
                    testResources,
                    ProductType.TEST_RESOURCE,
                    schematic.coordinates()
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CopyResourcesTask(
                    ProductType.EXPLODED_TEST_MODULE,
                    ProductType.TEST_RESOURCE,
                    schematic.coordinates(),
                    testResources
                )
            )
        );
    }

    private Path resourcesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("resources");
    }
}
