#!/bin/bash -xe

# Generate RSA user key
ssh-keygen -t rsa -N '' -f ~/.ssh/id_rsa
cp -av ~/.ssh/id_rsa{,_passphrase}
ssh-keygen -p -N pass1234 -f ~/.ssh/id_rsa_passphrase

# Generate ECDSA user key
ssh-keygen -t ecdsa -N '' -f ~/.ssh/id_ecdsa

# Register user keys
cat ~/.ssh/id_*.pub >> ~/.ssh/authorized_keys

# Generate known_hosts for RSA host key
rm -f ~/.ssh/known_hosts
ssh \
  -o UserKnownHostsFile=~/.ssh/known_hosts \
  -o HostKeyAlgorithms=ssh-rsa \
  -o StrictHostKeyChecking=no \
  -i ~/.ssh/id_rsa \
  localhost id
ssh-keygen -F localhost -f ~/.ssh/known_hosts

# Generate known_hosts for ECDSA host key
rm -f ~/.ssh/known_hosts_ecdsa
ssh \
  -o UserKnownHostsFile=~/.ssh/known_hosts_ecdsa \
  -o HostKeyAlgorithms=ecdsa-sha2-nistp256 \
  -o StrictHostKeyChecking=no \
  -i ~/.ssh/id_rsa \
  localhost id
ssh-keygen -F localhost -f ~/.ssh/known_hosts_ecdsa
