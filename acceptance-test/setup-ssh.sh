#!/bin/bash -xe

BUILD_DIR="$(dirname $0)/fixture/build"

rm -fr "$BUILD_DIR/.ssh"
mkdir -p "$BUILD_DIR/.ssh"

ssh-keygen -t rsa -N '' -f "$BUILD_DIR/.ssh/id_rsa"

docker run -d \
  --name gradle-ssh-plugin-integration-test-box \
  -e authorized_key="$(cat "$BUILD_DIR/.ssh/id_rsa.pub")" \
  -p 8022:22 \
  int128/integration-test-box:latest
