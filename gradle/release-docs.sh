#!/bin/bash -xe
HUB=2.2.0
PR_REPO="gradle-ssh-plugin/gradle-ssh-plugin.github.io"
PR_BRANCH="${TRAVIS_BRANCH}-${TRAVIS_BUILD_NUMBER}"

# Set credential
mkdir -p "$HOME/.config"
set +x
echo "https://${GH_TOKEN}:@github.com" > "$HOME/.config/git-credential"
echo "github.com:
- oauth_token: $GH_TOKEN
  user: $GH_USER" > "$HOME/.config/hub"
unset GH_TOKEN
set -x

# Configure git
git config --global user.name  "${GH_USER}"
git config --global user.email "${GH_USER}@users.noreply.github.com"
git config --global core.autocrlf "input"
git config --global hub.protocol "https"
git config --global credential.helper "store --file=$HOME/.config/git-credential"

# Install hub
curl -LO "https://github.com/github/hub/releases/download/v$HUB/hub-linux-amd64-$HUB.tar.gz"
tar -C "$HOME" -zxf "hub-linux-amd64-$HUB.tar.gz"
export PATH="$PATH:$HOME/hub-linux-amd64-$HUB"

# Open a pull request for the document
hub clone "$PR_REPO" _
cd _
hub checkout -b "$PR_BRANCH"
hub rm -r docs
cp -a ../build/asciidoc/html5 docs
hub add .
hub commit -m "Release from $TRAVIS_BRANCH"
hub push origin "$PR_BRANCH"
hub pull-request -m "Release from $TRAVIS_BRANCH"
cd ..
