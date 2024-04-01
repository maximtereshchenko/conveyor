package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.nio.file.*;
import java.util.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorSchematic schematic, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.ARCHIVE,
                Step.RUN,
                (conveyorSchematic, products) -> Set.of(
                    schematic.product(
                        schematic.discoveryDirectory().resolve(configuration.get("path")),
                        ProductType.MODULE
                    )
                )
            )
        );
    }
}
