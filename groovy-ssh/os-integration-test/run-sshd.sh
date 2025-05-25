#!/bin/bash -xe

exec docker run --rm -p 22:22 \
  -e "SSH_HOST_DSA_KEY=$(cat etc/ssh/ssh_host_dsa_key)" \
  -e "SSH_HOST_RSA_KEY=$(cat etc/ssh/ssh_host_rsa_key)" \
  -e "SSH_HOST_ECDSA_KEY=$(cat etc/ssh/ssh_host_ecdsa_key)" \
  -e "SSH_HOST_ED25519_KEY=$(cat etc/ssh/ssh_host_ed25519_key)" \
  -e "SSH_AUTHORIZED_KEYS=$(cat etc/ssh/id_rsa.pub etc/ssh/id_rsa_pass.pub etc/ssh/id_ecdsa.pub)" \
  --name sshd \
  int128/sshd
