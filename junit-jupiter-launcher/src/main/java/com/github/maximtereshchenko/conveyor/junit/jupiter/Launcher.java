package com.github.maximtereshchenko.conveyor.junit.jupiter;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public final class Launcher {

    public static void main(String[] args) {
        var failureTestExecutionListener = new FailureTestExecutionListener();
        LauncherFactory.create()
            .execute(
                launcherDiscoveryRequest(Paths.get(args[0])),
                new ReportingTestExecutionListener(),
                failureTestExecutionListener
            );
        if (!failureTestExecutionListener.isSuccess()) {
            System.exit(1);
        }
    }

    private static LauncherDiscoveryRequest launcherDiscoveryRequest(Path testClassesDirectory) {
        return LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(Set.of(testClassesDirectory)))
            .build();
    }
}
