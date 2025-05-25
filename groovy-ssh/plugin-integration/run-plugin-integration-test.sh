#!/bin/bash -xe

function checkout_remote_branch () {
  local branch_name="$1"
  git fetch origin -v "$branch_name:$branch_name"
  git checkout "$branch_name"
}

cd "$(dirname $0)/gradle-ssh-plugin"
git reset --hard

if checkout_remote_branch groovy-ssh-acceptance-test
then
  echo 'Use dedicated branch for specification change breaking backward compatibility'
fi

sed -i -e "s,groovy-ssh:[0-9.]*,groovy-ssh:${CIRCLE_TAG:-SNAPSHOT},g" core/build.gradle
echo 'repositories.mavenLocal()' >> core/build.gradle

mkdir -p acceptance-test/fixture/build
cp -av ../../os-integration-test/build/.ssh acceptance-test/fixture/build/.ssh

./gradlew -Ptarget.gradle.versions=1.12 :acceptance-test:test
