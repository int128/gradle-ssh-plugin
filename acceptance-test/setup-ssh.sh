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
sleep 1

ssh -o HostKeyAlgorithms=ssh-rsa \
  -o UserKnownHostsFile="$BUILD_DIR/.ssh/known_hosts" \
  -o StrictHostKeyChecking=no \
  -i "$BUILD_DIR/.ssh/id_rsa" \
  -p 8022 \
  tester@sandbox.127.0.0.1.xip.io id

ssh-keygen -F '[sandbox.127.0.0.1.xip.io]:8022' -f "$BUILD_DIR/.ssh/known_hosts"
