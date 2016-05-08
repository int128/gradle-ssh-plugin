#!/bin/bash -xe

GRADLE_VERSION="$1"
if [ "$GRADLE_VERSION" ]; then
  sed -i -e "s,gradle-[0-9.]*-,gradle-${GRADLE_VERSION}-,g" gradle/wrapper/gradle-wrapper.properties
fi

./gradlew --version
./gradlew -p acceptance-test -s --continue test

git reset --hard
