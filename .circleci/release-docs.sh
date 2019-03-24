#!/bin/bash -xe

./gradlew asciidoctor

ghcp -u gradle-ssh-plugin -r docs -b gh-pages \
  -m "Published by $CIRCLE_BUILD_URL" \
  -C docs/build/asciidoc/html5 \
  --dry-run .
