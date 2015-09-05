#!/bin/bash -xe

test "$TRAVIS_BRANCH" = "master"

PR_SOURCE="docs/build/asciidoc/html5"
PR_REPO="gradle-ssh-plugin/gradle-ssh-plugin.github.io"
PR_BRANCH="travis-${TRAVIS_BUILD_NUMBER}"
PR_MESSAGE="Release document"

test -d "$PR_SOURCE"

hub clone "$PR_REPO" _
cd _
hub checkout -b "$PR_BRANCH"
hub rm -r docs
cp -a "../$PR_SOURCE" docs
hub add .
hub commit -m "Document from $TRAVIS_REPO_SLUG:$TRAVIS_BRANCH $TRAVIS_COMMIT_RANGE"
hub push origin "$PR_BRANCH"
hub pull-request -m "Document from $TRAVIS_REPO_SLUG:$TRAVIS_BRANCH"
cd ..
