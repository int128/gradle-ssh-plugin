Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh)
==========

Groovy SSH is an automation tool based on DSL providing the remote command execution and file transfer.


DSL example
-----------

Here is an example script for the typical deployment scenario.

```groovy
// deploy.groovy
ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put from: 'example.war', into: '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```

See [document of Gradle SSH Plugin](https://gradle-ssh-plugin.github.io) for details of DSL.


Run the DSL script
------------------

We have following methods to run the script.

### Homebrew

Install `gssh` from Homebrew and run the script.

```sh
brew install gssh
gssh deploy.groovy
```

### Download the release

Download the latest `gssh.jar` from [releases](https://github.com/int128/groovy-ssh/releases) and run the script.

```sh
java -jar gssh.jar deploy.groovy
```

### Grape

Add following header to the script for using [Grape](http://groovy.codehaus.org/Grape),

```groovy
@Grab('org.hidetake:groovy-ssh:x.y.z')
@Grab('ch.qos.logback:logback-classic:1.1.2')
def ssh = org.hidetake.groovy.ssh.Ssh.newService()
```

and run the script on Groovy.

```sh
groovy deploy.groovy
```


Embed the Library in your App
-----------------------------

We can embed Groovy SSH library in the Groovy application.

The library is available
on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22groovy-ssh%22)
and [Bintray](https://bintray.com/int128/maven/groovy-ssh).

```groovy
// Gradle
compile 'org.hidetake:groovy-ssh:x.y.z'
```

Instantiate a [Service](src/main/groovy/org/hidetake/groovy/ssh/core/Service.groovy)
by [`Ssh#newService()`](src/main/groovy/org/hidetake/groovy/ssh/Ssh.groovy)
and run the script as follows.

```groovy
import org.hidetake.groovy.ssh.Ssh
def ssh = Ssh.newService()

ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put from: 'example.war', into: '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```


Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Feel free to open issues or pull requests.
