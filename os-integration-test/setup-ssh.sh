#!/bin/bash -xe

BUILD_DIR="$(dirname $0)/build"

mkdir -p "$BUILD_DIR"
rm -f "$BUILD_DIR/id_rsa"
rm -f "$BUILD_DIR/id_rsa_passphrase"
rm -f "$BUILD_DIR/id_rsa.pub"
rm -f "$BUILD_DIR/known_hosts"
rm -f "$BUILD_DIR/known_hosts_ecdsa"

ssh-keygen -t rsa -N '' -f "$BUILD_DIR/id_rsa"

docker stop groovy-ssh-integration-test-box || true
docker rm groovy-ssh-integration-test-box || true
docker run -d --name groovy-ssh-integration-test-box -e authorized_key="$(cat "$BUILD_DIR/id_rsa.pub")" -p 8022:22 int128/integration-test-box:latest
sleep 1

# generate known_hosts with RSA
ssh -o HostKeyAlgorithms=ssh-rsa -o UserKnownHostsFile="$BUILD_DIR/known_hosts" -o StrictHostKeyChecking=no -i "$BUILD_DIR/id_rsa" -p 8022 tester@sandbox.127.0.0.1.xip.io id
ssh-keygen -F '[sandbox.127.0.0.1.xip.io]:8022' -f "$BUILD_DIR/known_hosts"

# generate known_hosts with ECDSA
ssh -o HostKeyAlgorithms=ecdsa-sha2-nistp256 -o UserKnownHostsFile="$BUILD_DIR/known_hosts_ecdsa" -o StrictHostKeyChecking=no -i "$BUILD_DIR/id_rsa" -p 8022 tester@sandbox.127.0.0.1.xip.io id
ssh-keygen -F '[sandbox.127.0.0.1.xip.io]:8022' -f "$BUILD_DIR/known_hosts_ecdsa"

# generate private key with passphrase
cp -av "$BUILD_DIR/id_rsa" "$BUILD_DIR/id_rsa_passphrase"
ssh-keygen -p -N pass1234 -f "$BUILD_DIR/id_rsa_passphrase"
