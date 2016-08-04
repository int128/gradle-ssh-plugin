Gradle SSH Plugin [![CircleCI](https://circleci.com/gh/int128/gradle-ssh-plugin.svg?style=shield)](https://circleci.com/gh/int128/gradle-ssh-plugin) [![Gradle Status](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status.svg?branch=master)](https://gradleupdate.appspot.com/int128/gradle-ssh-plugin/status)
=================

Gradle SSH Plugin provides SSH facilities such as command execution or file transfer on Gradle.

https://gradle-ssh-plugin.github.io


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.

### Development

Gradle SSH Plugin internally uses [Groovy SSH](https://github.com/int128/groovy-ssh) library.
It depends on [JSch](http://www.jcraft.com/jsch/).

The document is maintained on the repository of Groovy SSH.

#### Acceptance Test

We can run acceptance tests to verify behavior of the plugin on Gradle environment.
It expects a SSH server is running at localhost port 8022.

Test condition can be set by system properties as follows:

System Property             | Value                         | Default
----------------------------|-------------------------------|--------
`target.gradle.versions`    | List of target Gradle version | current version and 1.12
`target.java.home`          | Target JVM                    | current JVM

e.g.

```sh
./gradlew -Ptarget.gradle.versions=3.0,2.0,1.12 :acceptance-tests:test
```

### Release

Push a versioned tag to GitHub and CI will upload the artifact to Bintray.


License
-------

```
Copyright 2012-2016 Hidetake Iwata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

