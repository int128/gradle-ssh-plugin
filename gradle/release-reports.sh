#!/bin/bash -xe

hub clone gradle-ssh-plugin/reports _
cd _

hub rm -fr *
for component in cli core os-integration-test server-integration-test
do
    if [ -d ../$component/build/reports/tests ]
    then
        cp -av ../$component/build/reports/tests $component
    fi
done

hub add .
hub commit -m "Build reports from $TRAVIS_BRANCH of $TRAVIS_REPO_SLUG"
hub push origin gh-pages

cd ..
rm -fr _
