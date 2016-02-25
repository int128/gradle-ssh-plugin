#!/bin/bash -xe

git clone https://github.com/gradle-ssh-plugin/reports _
cd _

git rm -fr *
for component in cli core os-integration-test server-integration-test
do
    if [ -d ../$component/build/reports/tests ]
    then
        cp -av ../$component/build/reports/tests $component
    fi
done

git add .
git commit -m "Build reports from $TRAVIS_BRANCH of $TRAVIS_REPO_SLUG"
git push origin gh-pages

cd ..
rm -fr _
