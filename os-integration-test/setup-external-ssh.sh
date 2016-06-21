#!/bin/bash -xe

test -f ~/.ssh/id_ext

# Generate known_hosts with RSA host key
rm -f ~/.ssh/known_hosts_ext
ssh \
  -o HostKeyAlgorithms=ssh-rsa \
  -o UserKnownHostsFile=~/.ssh/known_hosts_ext \
  -o StrictHostKeyChecking=no \
  -i ~/.ssh/id_ext \
  "${EXT_SSH_USER}@${EXT_SSH_HOST}" id
ssh-keygen -F "$EXT_SSH_HOST" -f ~/.ssh/known_hosts_ext

# Generate known_hosts with ECDSA host key
rm -f ~/.ssh/known_hosts_ext_ecdsa
ssh \
  -o HostKeyAlgorithms=ecdsa-sha2-nistp256 \
  -o UserKnownHostsFile=~/.ssh/known_hosts_ext_ecdsa \
  -o StrictHostKeyChecking=no \
  -i ~/.ssh/id_ext \
  "${EXT_SSH_USER}@${EXT_SSH_HOST}" id
ssh-keygen -F "$EXT_SSH_HOST" -f ~/.ssh/known_hosts_ext_ecdsa

# Generate private key with passphrase
cp -av ~/.ssh/id_ext{,_passphrase}
ssh-keygen -p -N pass1234 -f ~/.ssh/id_ext_passphrase
