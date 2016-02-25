#!/bin/bash -xe

test "$TRAVIS_BRANCH" = "master"

git clone https://github.com/gradle-ssh-plugin/docs _
cd _

git rm -fr *
cp -av ../docs/build/asciidoc/html5/* .

git add .
git commit -m "Documents from $TRAVIS_BRANCH of $TRAVIS_REPO_SLUG"
git push origin gh-pages

cd ..
rm -fr _
