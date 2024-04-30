package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

final class ReportingTestExecutionListener implements TestExecutionListener {

    private static final System.Logger LOGGER =
        System.getLogger(ReportingTestExecutionListener.class.getName());

    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        LOGGER.log(
            System.Logger.Level.INFO,
            "Tests run: total {0}, passed {1}, failed {2}, skipped {3}",
            passed.get() + failed.get() + skipped.get(),
            passed.get(),
            failed.get(),
            skipped.get()
        );
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        methodSource(testIdentifier)
            .ifPresent(methodSource -> onSkipped(testIdentifier, methodSource, reason));
    }

    @Override
    public void executionFinished(
        TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult
    ) {
        methodSource(testIdentifier)
            .ifPresent(methodSource ->
                onFinished(testIdentifier, methodSource, testExecutionResult)
            );
    }

    private void onSkipped(
        TestIdentifier testIdentifier,
        MethodSource methodSource,
        String reason
    ) {
        LOGGER.log(
            System.Logger.Level.INFO,
            () -> message(testIdentifier, methodSource, "SKIPPED (%s)".formatted(reason))
        );
    }

    private void onFinished(
        TestIdentifier testIdentifier,
        MethodSource methodSource,
        TestExecutionResult testExecutionResult
    ) {
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> {
                LOGGER.log(
                    System.Logger.Level.INFO,
                    () -> message(testIdentifier, methodSource, "OK")
                );
                passed.incrementAndGet();
            }
            case ABORTED -> {
                //empty
            }
            case FAILED -> {
                LOGGER.log(
                    System.Logger.Level.INFO,
                    () -> message(testIdentifier, methodSource, "FAILED"),
                    testExecutionResult.getThrowable()
                        .map(throwable -> withTrimmedStackTrace(throwable, methodSource))
                        .orElse(null)
                );
                failed.incrementAndGet();
            }
        }
    }

    private String message(
        TestIdentifier testIdentifier,
        MethodSource methodSource,
        String status
    ) {
        return "%s - %s - %s".formatted(
            methodSource.getClassName(),
            testIdentifier.getDisplayName(),
            status
        );
    }

    private Optional<MethodSource> methodSource(TestIdentifier testIdentifier) {
        return testIdentifier.getSource()
            .filter(MethodSource.class::isInstance)
            .map(MethodSource.class::cast);
    }

    private Throwable withTrimmedStackTrace(Throwable throwable, MethodSource methodSource) {
        var stackTrace = throwable.getStackTrace();
        throwable.setStackTrace(Arrays.copyOf(stackTrace, length(stackTrace, methodSource)));
        return throwable;
    }

    private int length(StackTraceElement[] stackTrace, MethodSource methodSource) {
        for (int i = 0; i < stackTrace.length; i++) {
            var element = stackTrace[i];
            if (
                element.getClassName().equals(methodSource.getClassName()) &&
                element.getMethodName().equals(methodSource.getMethodName())
            ) {
                return i + 1;
            }
        }
        return stackTrace.length;
    }
}
