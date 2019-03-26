#!/bin/bash -xe

keys="$(dirname "$0")/keys"

exec docker run --rm -p 22:22 \
  -e "SSH_HOST_DSA_KEY=$(cat $keys/etc/ssh/ssh_host_dsa_key)" \
  -e "SSH_HOST_RSA_KEY=$(cat $keys/etc/ssh/ssh_host_rsa_key)" \
  -e "SSH_HOST_ECDSA_KEY=$(cat $keys/etc/ssh/ssh_host_ecdsa_key)" \
  -e "SSH_HOST_ED25519_KEY=$(cat $keys/etc/ssh/ssh_host_ed25519_key)" \
  -e "SSH_AUTHORIZED_KEYS=$(cat $keys/id_rsa.pub)" \
  --name sshd \
  int128/sshd
