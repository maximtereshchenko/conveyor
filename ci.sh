#!/usr/bin/env bash

set -Eeuo pipefail

rm -rf ./.conveyor-cache
rm -rf ./.conveyor-repository
rm -rf ./.gradle-repository
rm -f ./conveyor-cli-1.0.0-executable.jar
./gradlew clean test
./gradlew installConveyorComponent
./gradlew conveyor-cli:standaloneJar
java -jar ./conveyor-cli/build/libs/conveyor-cli-1.0.0-standalone.jar ./conveyor.json publish
rm -r ./.gradle-repository
mv ./conveyor-cli/.conveyor/conveyor-cli-1.0.0-executable.jar ./conveyor-cli-1.0.0-executable.jar
java -jar ./conveyor-cli-1.0.0-executable.jar ./conveyor.json publish
