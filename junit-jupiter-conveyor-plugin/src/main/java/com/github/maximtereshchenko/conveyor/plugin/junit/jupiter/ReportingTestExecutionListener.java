package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.stream.Stream;

final class ReportingTestExecutionListener implements TestExecutionListener {

    @Override
    public void executionFinished(
        TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult
    ) {
        if (!testIdentifier.isTest()) {
            return;
        }
        reportTestResult(testIdentifier, testExecutionResult);
        reportThrowable(testIdentifier, testExecutionResult);
    }

    private void reportThrowable(
        TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult
    ) {
        testExecutionResult.getThrowable()
            .ifPresent(throwable ->
                System.out.printf(
                    "%s: %s%n  at %s%n",
                    throwable.getClass().getName(),
                    throwable.getLocalizedMessage(),
                    testStackTraceElement(
                        throwable,
                        testIdentifier.getSource()
                            .map(MethodSource.class::cast)
                            .orElseThrow()
                    )
                )
            );
    }

    private StackTraceElement testStackTraceElement(
        Throwable throwable,
        MethodSource methodSource
    ) {
        return Stream.of(throwable.getStackTrace())
            .filter(stackTraceElement ->
                stackTraceElement.getClassName()
                    .equals(methodSource.getClassName())
            )
            .filter(stackTraceElement ->
                stackTraceElement.getMethodName()
                    .equals(methodSource.getMethodName())
            )
            .findAny()
            .orElseThrow();
    }

    private void reportTestResult(
        TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult
    ) {
        System.out.printf(
            "%s - %s%n",
            testIdentifier.getDisplayName(),
            testExecutionResult.getStatus() == TestExecutionResult.Status.SUCCESSFUL ?
                "OK" :
                "FAILED"
        );
    }
}
