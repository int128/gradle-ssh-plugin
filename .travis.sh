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

function publish_report () {
    if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
        set +x
        echo "machine github.com" >> ~/.netrc
        echo "login $GH_LOGIN"    >> ~/.netrc
        echo "password $GH_TOKEN" >> ~/.netrc
        set -x

        git config --global user.email 'travis@travis-ci.org'
        git config --global user.name 'travis'
        git clone --quiet --branch=gh-pages "https://github.com/$GH_BUILD_REPORT.git" gh-pages
        cd gh-pages

        git rm -q -r "$TRAVIS_BRANCH" || true
        mkdir -p "$TRAVIS_BRANCH"

        [ -d ../build/reports ] && cp -a ../build/reports "$TRAVIS_BRANCH"
        [ -d ../build/docs    ] && cp -a ../build/docs    "$TRAVIS_BRANCH"

        git add "$TRAVIS_BRANCH"
        git commit -q -m "Automatically updated by Travis build $TRAVIS_BUILD_NUMBER"
        git push origin gh-pages

        rm -v ~/.netrc
    fi
}

"$@"
