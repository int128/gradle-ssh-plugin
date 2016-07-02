#!/bin/bash -xe

git clone --depth 1 https://github.com/int128/gradle-ssh-plugin
cd gradle-ssh-plugin

git checkout -b "groovy-ssh-$CIRCLE_TAG"
sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:${CIRCLE_TAG:-SNAPSHOT},g" core/build.gradle
git add .
git commit -m "Groovy SSH $CIRCLE_TAG"

./gradlew -Ptarget.gradle.versions=1.12 check
