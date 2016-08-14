#!/bin/bash -xe

cd "$(dirname $0)/gradle-ssh-plugin"
git reset --hard

sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:${CIRCLE_TAG:-SNAPSHOT},g" core/build.gradle
echo 'repositories.mavenLocal()' >> core/build.gradle

mkdir -p acceptance-test/fixture/build
cp -av ../../os-integration-test/build/id_rsa acceptance-test/fixture/build
cp -av ../../os-integration-test/build/known_hosts acceptance-test/fixture/build

./gradlew -Ptarget.gradle.versions=1.12 :acceptance-test:test
