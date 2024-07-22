package com.github.maximtereshchenko.conveyor.junit.jupiter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

final class ReportingTestExecutionListener implements TestExecutionListener {

    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger skipped = new AtomicInteger();

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.printf(
            ">Tests run: total %d, passed %d, failed %d, skipped %d%n",
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
        report(testIdentifier, methodSource, "SKIPPED (%s)".formatted(reason));
    }

    private void onFinished(
        TestIdentifier testIdentifier,
        MethodSource methodSource,
        TestExecutionResult testExecutionResult
    ) {
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> {
                report(testIdentifier, methodSource, "OK");
                passed.incrementAndGet();
            }
            case ABORTED -> {
                //empty
            }
            case FAILED -> {
                report(testIdentifier, methodSource, "FAILED");
                testExecutionResult.getThrowable()
                    .map(throwable -> withTrimmedStackTrace(throwable, methodSource))
                    .ifPresent(throwable -> throwable.printStackTrace(System.out));
                failed.incrementAndGet();
            }
        }
    }

    private void report(
        TestIdentifier testIdentifier,
        MethodSource methodSource,
        String status
    ) {
        System.out.printf(
            ">%s - %s - %s%n",
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
