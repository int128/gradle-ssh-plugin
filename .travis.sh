#!/bin/bash -xe

function acceptance_test () {
    ssh-keygen -t rsa -N '' -C '' -f ~/.ssh/id_rsa
    ssh-keygen -t rsa -N 'pass_phrase' -C '' -f ~/.ssh/id_rsa_pass
    tee -a ~/.ssh/authorized_keys < ~/.ssh/id_rsa.pub
    ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa localhost true
    ./gradlew -i -p acceptance-test test aggressiveTest
}

function publish_report () {
    case "$TRAVIS_BRANCH" in
    "master" | "travis")
        product_version="$(grep version= gradle.properties | cut -d= -f2)"
        [ -n "$product_version" ]

        set +x
        echo "machine github.com" >> ~/.netrc
        echo "login $GH_LOGIN"    >> ~/.netrc
        echo "password $GH_TOKEN" >> ~/.netrc
        set -x

        git config --global user.email 'travis@travis-ci.org'
        git config --global user.name 'travis'
        git clone --quiet --branch=gh-pages "https://github.com/$TRAVIS_REPO_SLUG.git" gh-pages
        cd gh-pages

        git rm -r "build/$product_version" || true
        mkdir -p "build/$product_version"
        cp -a ../build/reports "build/$product_version"
        cp -a ../build/docs    "build/$product_version"
        git add "build/$product_version"
        git commit -m "Automatically updated (Travis build $TRAVIS_BUILD_NUMBER)"
        git push origin gh-pages

        rm -v ~/.netrc
        ;;
    esac
}

"$@"
