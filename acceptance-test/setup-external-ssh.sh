#!/bin/bash -xe

# Generate known_hosts for the external SSH server
rm -f ~/.ssh/known_hosts_ext
ssh \
  -o UserKnownHostsFile=~/.ssh/known_hosts_ext \
  -o StrictHostKeyChecking=no \
  -i ~/.ssh/id_ext \
  "${EXT_SSH_USER}@${EXT_SSH_HOST}" id
ssh-keygen -F "$EXT_SSH_HOST" -f ~/.ssh/known_hosts_ext
