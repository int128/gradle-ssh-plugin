#!/bin/bash -xe
#
# Run acceptance tests on Travis CI.
#
# Environment Variables:
# - GRADLE_VERSION: Gradle version (mandatory)
# - version: Product version (SNAPSHOT version if not given)
#
# Requirements:
# - sshd must be running
# - sudo must be enabled without password
# - java must be installed

cd $(dirname $0)
test "$GRADLE_VERSION"

# install Gradle
curl -LO "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
unzip -q "gradle-${GRADLE_VERSION}-bin.zip"
chmod +x "gradle-${GRADLE_VERSION}/bin/gradle"
ln -vsf  "gradle-${GRADLE_VERSION}/bin/gradle"
./gradle --version

# enable public key authentication
mkdir -m 700 -p -v $HOME/.ssh
[ -f $HOME/.ssh/id_rsa ]          || ssh-keygen -t rsa -N ''            -f $HOME/.ssh/id_rsa
[ -f $HOME/.ssh/id_rsa_pass ]     || ssh-keygen -t rsa -N 'pass_phrase' -f $HOME/.ssh/id_rsa_pass
[ -f $HOME/.ssh/authorized_keys ] || cat $HOME/.ssh/id_rsa.pub           > $HOME/.ssh/authorized_keys

# generate a known hosts file
ssh -o StrictHostKeyChecking=no \
    -o HostKeyAlgorithms=ssh-rsa \
    -o UserKnownHostsFile=$HOME/.ssh/known_hosts \
    -i $HOME/.ssh/id_rsa \
    localhost id
ssh-keygen -H -F localhost

# run tests
./gradle -s -Pversion="${version:-SNAPSHOT}" test testWithSideEffect

# run tests with ssh-agent
eval $(ssh-agent)
ssh-add $HOME/.ssh/id_rsa
./gradle -s -Pversion="${version:-SNAPSHOT}" testWithSshAgent
ssh-agent -k
