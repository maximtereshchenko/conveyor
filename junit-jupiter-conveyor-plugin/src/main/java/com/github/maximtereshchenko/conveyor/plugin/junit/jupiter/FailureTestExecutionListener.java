package com.github.maximtereshchenko.conveyor.plugin.junit.jupiter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

final class FailureTestExecutionListener implements TestExecutionListener {

    private boolean success = true;

    @Override
    public void executionFinished(
        TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult
    ) {
        if (
            testIdentifier.isTest() &&
            testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED
        ) {
            success = false;
        }
    }

    boolean isSuccess() {
        return success;
    }
}
