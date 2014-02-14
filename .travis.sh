#!/bin/bash -xe

function acceptance_test () {
    exec > >(tee "build/reports/acceptance-test.log") 2>&1

    ssh-keygen -t rsa -N '' -C '' -f ~/.ssh/id_rsa
    ssh-keygen -t rsa -N 'pass_phrase' -C '' -f ~/.ssh/id_rsa_pass
    tee -a ~/.ssh/authorized_keys < ~/.ssh/id_rsa.pub

    ssh -o StrictHostKeyChecking=no \
        -o UserKnownHostsFile=~/.ssh/known_hosts \
        -o HashKnownHosts=no \
        -o HostKeyAlgorithms=ssh-rsa \
        -i ~/.ssh/id_rsa localhost true
    grep localhost ~/.ssh/known_hosts

    mkdir -p build/reports
    ./gradlew -i -s -p acceptance-test test aggressiveTest
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
        git clone --quiet --branch=gh-pages "https://github.com/$TRAVIS_REPO_SLUG.git" gh-pages
        cd gh-pages

        git rm -r "build/$TRAVIS_BRANCH" || true
        mkdir -p "build/$TRAVIS_BRANCH"
        cp -a ../build/reports "build/$TRAVIS_BRANCH"
        cp -a ../build/docs    "build/$TRAVIS_BRANCH"
        git add "build/$TRAVIS_BRANCH"
        git commit -m "Automatically updated (Travis build $TRAVIS_BUILD_NUMBER)"
        git push origin gh-pages

        rm -v ~/.netrc
    fi
}

"$@"
