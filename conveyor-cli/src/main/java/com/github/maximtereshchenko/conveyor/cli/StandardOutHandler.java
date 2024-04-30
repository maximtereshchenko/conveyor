package com.github.maximtereshchenko.conveyor.cli;

import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public final class StandardOutHandler extends StreamHandler {

    public StandardOutHandler() {
        setOutputStream(System.out);
    }

    @Override
    public void publish(LogRecord logRecord) {
        super.publish(logRecord);
        flush();
    }

    @Override
    public void close() {
        flush();
    }
}
