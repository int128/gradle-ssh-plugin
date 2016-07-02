#!/bin/bash -xe

rm -f ~/.ssh/id_ext
ssh-keygen -t rsa -N '' -f ~/.ssh/id_ext

docker run -d -e authorized_key="$(cat ~/.ssh/id_ext.pub)" -p 8022:22 int128/integration-test-box:latest
sleep 1

# generate known_hosts with RSA
rm -f ~/.ssh/known_hosts_ext
ssh -o HostKeyAlgorithms=ssh-rsa -o UserKnownHostsFile=~/.ssh/known_hosts_ext -o StrictHostKeyChecking=no -i ~/.ssh/id_ext -p 8022 tester@sandbox.local id
ssh-keygen -F '[sandbox.local]:8022' -f ~/.ssh/known_hosts_ext

# generate known_hosts with ECDSA
rm -f ~/.ssh/known_hosts_ext_ecdsa
ssh -o HostKeyAlgorithms=ecdsa-sha2-nistp256 -o UserKnownHostsFile=~/.ssh/known_hosts_ext_ecdsa -o StrictHostKeyChecking=no -i ~/.ssh/id_ext -p 8022 tester@sandbox.local id
ssh-keygen -F '[sandbox.local]:8022' -f ~/.ssh/known_hosts_ext_ecdsa

# generate private key with passphrase
cp -av ~/.ssh/id_ext{,_passphrase}
ssh-keygen -p -N pass1234 -f ~/.ssh/id_ext_passphrase
