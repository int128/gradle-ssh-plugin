#!/bin/bash -xe

BUILD_DIR="$(dirname $0)/build"

rm -f "$BUILD_DIR/.ssh"
mkdir -p "$BUILD_DIR/.ssh"

# generate private key
ssh-keygen -t rsa -N '' -f "$BUILD_DIR/.ssh/id_rsa"

# generate private key with passphrase
cp -av "$BUILD_DIR/.ssh/id_rsa" "$BUILD_DIR/.ssh/id_rsa_passphrase"
ssh-keygen -p -N pass1234 -f "$BUILD_DIR/.ssh/id_rsa_passphrase"

docker run -d \
  --name groovy-ssh-integration-test-internal-box \
  -e authorized_key="$(cat "$BUILD_DIR/.ssh/id_rsa.pub")" \
  int128/integration-test-box:latest

docker run -d \
  --name groovy-ssh-integration-test-box \
  --link groovy-ssh-integration-test-internal-box \
  -e authorized_key="$(cat "$BUILD_DIR/.ssh/id_rsa.pub")" \
  -p 8022:22 \
  int128/integration-test-box:latest

sleep 1

# generate known_hosts by OpenSSH
ssh -o HostKeyAlgorithms=ecdsa-sha2-nistp256 \
  -o UserKnownHostsFile="$BUILD_DIR/.ssh/known_hosts_openssh" \
  -o StrictHostKeyChecking=no \
  -i "$BUILD_DIR/.ssh/id_rsa" \
  -p 8022 \
  tester@sandbox.127.0.0.1.xip.io id

ssh-keygen -F '[sandbox.127.0.0.1.xip.io]:8022' -f "$BUILD_DIR/.ssh/known_hosts_openssh"
