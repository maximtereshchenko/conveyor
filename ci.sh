#!/usr/bin/env bash

set -Eeuo pipefail

./gradlew clean test
./gradlew installConveyorComponent
./gradlew conveyor-cli:standaloneJar
java -jar ./conveyor-cli/build/libs/conveyor-cli-1.0.0-standalone.jar ./conveyor.json PUBLISH
rm -rf ./.gradle-repository
java -jar ./conveyor-cli/build/libs/conveyor-cli-1.0.0-standalone.jar ./conveyor.json PUBLISH
