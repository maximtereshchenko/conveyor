package com.github.maximtereshchenko.conveyor.plugin.compile;

import com.github.maximtereshchenko.conveyor.common.api.ProductType;
import com.github.maximtereshchenko.conveyor.common.api.Stage;
import com.github.maximtereshchenko.conveyor.common.api.Step;
import com.github.maximtereshchenko.conveyor.compiler.Compiler;
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
        var compiler = new Compiler();
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.PREPARE,
                new DiscoverJavaFilesTask(
                    "discover-sources",
                    javaFilesDirectory(schematic.discoveryDirectory(), "main"),
                    ProductType.SOURCE,
                    schematic.coordinates()
                )
            ),
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                new CompileSourcesTask(
                    schematic,
                    schematic.constructionDirectory().resolve("exploded-jar"),
                    compiler
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new DiscoverJavaFilesTask(
                    "discover-test-sources",
                    javaFilesDirectory(schematic.discoveryDirectory(), "test"),
                    ProductType.TEST_SOURCE,
                    schematic.coordinates()
                )
            ),
            new ConveyorTaskBinding(
                Stage.TEST,
                Step.PREPARE,
                new CompileTestSourcesTask(
                    schematic,
                    schematic.constructionDirectory().resolve("exploded-test-jar"),
                    compiler
                )
            )
        );
    }

    private Path javaFilesDirectory(Path path, String sourceSet) {
        return path.resolve("src").resolve(sourceSet).resolve("java");
    }
}
