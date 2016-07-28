#!/bin/bash -xe

cd "$(dirname $0)/gradle-ssh-plugin"
git reset --hard

git checkout -b "groovy-ssh-$CIRCLE_TAG"
sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:${CIRCLE_TAG:-SNAPSHOT},g" core/build.gradle
git add .
git commit -m "Groovy SSH $CIRCLE_TAG"
git push origin
