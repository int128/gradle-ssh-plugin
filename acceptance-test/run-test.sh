#!/bin/bash -xe
#
# Requirements:
# - sshd must be running
# - sudo must be enabled without password
# - java must be installed

[ -z "$version" ] && version=SNAPSHOT

# determine Gradle path
[ -x ../gradlew ] && gradle=../gradlew
[ -x ./gradle/bin/gradle ] && gradle=./gradle/bin/gradle

# enable public key authentication
mkdir -m 700 -p -v                    $HOME/.ssh
ssh-keygen -t rsa -N ''            -f $HOME/.ssh/id_rsa
ssh-keygen -t rsa -N 'pass_phrase' -f $HOME/.ssh/id_rsa_pass
cat $HOME/.ssh/id_rsa.pub           > $HOME/.ssh/authorized_keys

# generate a known hosts file
ssh -o StrictHostKeyChecking=no \
    -o HostKeyAlgorithms=ssh-rsa \
    -o UserKnownHostsFile=$HOME/.ssh/known_hosts \
    -i $HOME/.ssh/id_rsa \
    localhost id
ssh-keygen -H -F localhost

# run tests
"$gradle" -i -s -Pversion="$version" test aggressiveTest

# run tests with ssh-agent
eval $(ssh-agent)
ssh-add $HOME/.ssh/id_rsa
"$gradle" -i -s -Pversion="$version" testWithAgent
ssh-agent -k
