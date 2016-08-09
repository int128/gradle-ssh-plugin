#!/bin/bash -xe

cd "$(dirname $0)/.."
./gradlew asciidoctor

cd "$(dirname $0)/build/asciidoc/html5"
git init
git add .
git commit -m "Release from CI"
git branch -m gh-pages
git remote add origin https://github.com/gradle-ssh-plugin/docs
git push -f origin gh-pages
