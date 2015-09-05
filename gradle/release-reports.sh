#!/bin/bash -xe

hub clone gradle-ssh-plugin/reports _
cd _

hub rm -fr *
cp -av ../cli/build/reports cli
cp -av ../core/build/reports core
cp -av ../os-integration-test/build/reports os-integration-test
cp -av ../server-integration-test/build/reports server-integration-test

hub add .
hub commit -m "Build reports from $TRAVIS_BRANCH of $TRAVIS_REPO_SLUG"
hub push origin gh-pages

cd ..
rm -fr _
