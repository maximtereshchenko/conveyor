package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorPlugin;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorSchematic;
import com.github.maximtereshchenko.conveyor.plugin.api.ConveyorTaskBinding;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class CompilePlugin implements ConveyorPlugin {

    @Override
    public String name() {
        return "compile-conveyor-plugin";
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
                new DiscoverJavaFilesTask(
                    java(src(schematic.discoveryDirectory()).resolve("main")),
                    ProductType.SOURCE,
                    schematic
                )
            ),
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                new CompileSourcesTask(
                    schematic,
                    schematic.constructionDirectory().resolve("exploded-module")
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new DiscoverJavaFilesTask(
                    java(src(schematic.discoveryDirectory()).resolve("test")),
                    ProductType.TEST_SOURCE,
                    schematic
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CompileTestSourcesTask(
                    schematic,
                    schematic.constructionDirectory().resolve("exploded-test-module")
                )
            )
        );
    }

    private Path src(Path path) {
        return path.resolve("src");
    }

    private Path java(Path path) {
        return path.resolve("java");
    }
}
