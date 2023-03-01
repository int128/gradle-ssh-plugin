# Gradle SSH Plugin [![build](https://github.com/int128/gradle-ssh-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/int128/gradle-ssh-plugin/actions/workflows/build.yaml)

Gradle SSH Plugin provides SSH facilities such as command execution or file transfer on Gradle.

https://gradle-ssh-plugin.github.io

## Contributions

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.

### Development

Gradle SSH Plugin internally uses [Groovy SSH](https://github.com/int128/groovy-ssh) library.
It depends on [JSch](http://www.jcraft.com/jsch/).

The document is maintained on the repository of Groovy SSH.

#### Acceptance Test

TODO: fix it

### Release

Create a new release in GitHub Releases.
GitHub Actions will publish an artifact to Gradle Plugin Portal.
