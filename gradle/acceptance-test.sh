#!/bin/bash -xe

./gradlew --version
./gradlew -p acceptance-test -s test

sed -i -e 's,gradle-[0-9.]*-,gradle-1.12-,g' gradle/wrapper/gradle-wrapper.properties
./gradlew --version
./gradlew -p acceptance-test -s test

git reset --hard
./gradlew --version
