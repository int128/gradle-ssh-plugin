#!/bin/bash -xe

rm -f ~/.ssh/id_ext
ssh-keygen -t rsa -N '' -f ~/.ssh/id_ext

docker run -d -e authorized_key="$(cat ~/.ssh/id_ext.pub)" -p 8022:22 int128/integration-test-box:latest
sleep 1

# generate known_hosts
rm -f ~/.ssh/known_hosts_ext
ssh -o UserKnownHostsFile=~/.ssh/known_hosts_ext -o StrictHostKeyChecking=no -i ~/.ssh/id_ext -p 8022 tester@sandbox.local id
ssh-keygen -F '[sandbox.local]:8022' -f ~/.ssh/known_hosts_ext
