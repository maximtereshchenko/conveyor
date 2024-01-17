package com.github.maximtereshchenko.conveyor.plugin.api;

import com.github.maximtereshchenko.conveyor.common.api.BuildFiles;

public interface ConveyorTask {

    BuildFiles execute(BuildFiles buildFiles);
}
