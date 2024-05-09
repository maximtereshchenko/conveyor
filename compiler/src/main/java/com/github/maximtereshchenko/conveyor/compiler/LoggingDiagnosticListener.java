package com.github.maximtereshchenko.conveyor.compiler;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

final class LoggingDiagnosticListener implements DiagnosticListener<JavaFileObject> {

    private static final System.Logger LOGGER =
        System.getLogger(LoggingDiagnosticListener.class.getName());

    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        LOGGER.log(System.Logger.Level.ERROR, diagnostic);
    }
}
