package com.github.maximtereshchenko.conveyor.files;

import java.io.IOException;

@FunctionalInterface
interface IOAction {

    void execute() throws IOException;
}
