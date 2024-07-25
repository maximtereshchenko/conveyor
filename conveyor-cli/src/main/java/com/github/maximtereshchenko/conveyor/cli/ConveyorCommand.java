package com.github.maximtereshchenko.conveyor.cli;

import com.github.maximtereshchenko.conveyor.api.Stage;
import com.github.maximtereshchenko.conveyor.api.TaskCache;
import com.github.maximtereshchenko.conveyor.api.TracingLevel;
import com.github.maximtereshchenko.conveyor.core.ConveyorFacade;
import com.github.maximtereshchenko.conveyor.jackson.JacksonAdapter;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@CommandLine.Command(
    name = "conveyor",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    showDefaultValues = true
)
final class ConveyorCommand implements Runnable {

    @CommandLine.Option(
        names = "--serial",
        description = "Construct schematics one after another"
    )
    private boolean serial;
    @CommandLine.Option(
        names = "--disable-task-cache",
        description = "Do not use task cache during construction"
    )
    private boolean taskCacheDisabled;
    @CommandLine.ArgGroup
    private TracingConfiguration tracingConfiguration = new TracingConfiguration();
    @CommandLine.Option(
        names = "--file",
        description = "Path to file with the schematic",
        defaultValue = "conveyor.json"
    )
    private Path path;
    @CommandLine.Parameters(arity = "1..*", description = "Target stages")
    private List<Stage> stages;

    @Override
    public void run() {
        ConveyorFacade.from(
                JacksonAdapter.configured(),
                executor(),
                taskCache(),
                System.out::println,
                tracingLevel()
            )
            .construct(path.toAbsolutePath().normalize(), stages);
    }

    private TaskCache taskCache() {
        if (taskCacheDisabled) {
            return TaskCache.DISABLED;
        }
        return TaskCache.ENABLED;
    }

    private Executor executor() {
        if (serial) {
            return Runnable::run;
        }
        return Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual()
                .name("virtual-", 1)
                .factory()
        );
    }

    private TracingLevel tracingLevel() {
        if (tracingConfiguration.silent) {
            return TracingLevel.SILENT;
        }
        if (tracingConfiguration.verbose) {
            return TracingLevel.VERBOSE;
        }
        return TracingLevel.NORMAL;
    }

    private static final class TracingConfiguration {

        @CommandLine.Option(
            names = "--silent",
            description = "Output only important information during the construction"
        )
        private boolean silent;
        @CommandLine.Option(
            names = "--verbose",
            description = "Output all available information during the construction"
        )
        private boolean verbose;
    }
}
