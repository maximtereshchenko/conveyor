package ${normalizedName};

import com.github.maximtereshchenko.conveyor.common.api.*;
import com.github.maximtereshchenko.conveyor.plugin.api.*;

import java.util.*;

public final class ${normalizedName} implements ConveyorPlugin {

    @Override
    public String name() {
        return "${name}";
    }

    @Override
    public List<ConveyorTaskBinding> bindings(ConveyorProperties properties, Map<String, String> configuration) {
        return List.of(
            new ConveyorTaskBinding(
                Stage.COMPILE,
                Step.RUN,
                (dependencies, products) -> products.with(properties.constructionDirectory(), ProductType.MODULE)
            )
        );
    }
}
