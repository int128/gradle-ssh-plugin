#!/bin/bash -xe

test "$TRAVIS_BRANCH" = "master"

hub clone gradle-ssh-plugin/docs _
cd _

hub rm -fr *
cp -av ../docs/build/asciidoc/html5/* .

hub add .
hub commit -m "Documents from $TRAVIS_BRANCH of $TRAVIS_REPO_SLUG"
hub push origin gh-pages

cd ..
rm -fr _
