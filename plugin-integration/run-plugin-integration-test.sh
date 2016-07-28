#!/bin/bash -xe

./gradlew publishToMavenLocal

cd "$(dirname $0)/gradle-ssh-plugin"
git reset --hard

sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:${CIRCLE_TAG:-SNAPSHOT},g" core/build.gradle
echo 'repositories.mavenLocal()' >> core/build.gradle
./gradlew check
