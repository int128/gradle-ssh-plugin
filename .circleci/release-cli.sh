#!/bin/bash -xe

./gradlew shadowJar

ghr -u int128 -r groovy-ssh \
  -n "$CIRCLE_TAG" \
  -b "Released on $(date +%Y-%m-%d)" \
  "$CIRCLE_TAG" \
  cli/build/libs/gssh.jar
