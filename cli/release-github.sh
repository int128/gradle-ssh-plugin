#!/bin/bash -xe

./gradlew shadowJar

export ARTIFACT="$(dirname $0)/build/libs/gssh.jar"
test -f "$ARTIFACT"

curl -LO https://github.com/aktau/github-release/releases/download/v0.6.2/linux-amd64-github-release.tar.bz2
tar -jxf linux-amd64-github-release.tar.bz2
export PATH="$PWD/bin/linux/amd64:$PATH"

github-release release \
  --user int128 \
  --repo groovy-ssh \
  --tag "$CIRCLE_TAG" \
  --name "$CIRCLE_TAG" \
  --description "Released on $(date +%Y-%m-%d)"

github-release upload \
  --user int128 \
  --repo groovy-ssh \
  --tag "$CIRCLE_TAG" \
  --name "$(basename "$ARTIFACT")" \
  --file "$ARTIFACT"
