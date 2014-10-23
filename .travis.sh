#!/bin/bash -xe

function acceptance_test () {
    mkdir -p build/reports

    ssh-keygen -t rsa -N '' -C '' -f ~/.ssh/id_rsa
    ssh-keygen -t rsa -N 'pass_phrase' -C '' -f ~/.ssh/id_rsa_pass
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

    ssh -o StrictHostKeyChecking=no \
        -o UserKnownHostsFile=~/.ssh/known_hosts \
        -o HostKeyAlgorithms=ssh-rsa \
        -i ~/.ssh/id_rsa localhost true
    ssh-keygen -H -F localhost

    ./gradlew -i -s -p acceptance-test test aggressiveTest >> "build/reports/acceptance-test.log"

    eval $(ssh-agent)
    ssh-add ~/.ssh/id_rsa
    ./gradlew -i -s -p acceptance-test testWithAgent >> "build/reports/acceptance-test.log"
}

"$@"
