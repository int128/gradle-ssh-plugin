Groovy SSH [![Build Status](https://travis-ci.org/int128/groovy-ssh.svg?branch=master)](https://travis-ci.org/int128/groovy-ssh)
==========

Groovy SSH is an automation tool based on intuitive DSL.

It provides the remote command execution and file transfer facilities.

See also [the user guide of Gradle SSH Plugin](https://gradle-ssh-plugin.github.io/user-guide.html) for details of DSL syntax.


Getting Started
---------------

Download the release and run. It requires Java 6 or later.

```sh
curl -L -O https://github.com/int128/groovy-ssh/releases/download/latest/groovy-ssh.jar
java -jar groovy-ssh.jar
```

Create a following script and save as `deploy.groovy`.

```groovy
ssh.remotes {
  webServer {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = new File('id_rsa')
  }
}

ssh.run {
  session(ssh.remotes.webServer) {
    put 'example.war', '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```

Run the tool with script path.

```sh
java -jar groovy-ssh.jar deploy.groovy
```

### Dockerized

The tool is available on Docker Hub. Run a container as follows:

```sh
docker run --rm int128/groovy-ssh
```

Embed SSH library in your App
-----------------------------

You can embed the SSH library in your Groovy application.

The library is available on [Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.hidetake%22%20AND%20a%3A%22groovy-ssh%22) and [Bintray](https://bintray.com/int128/maven/groovy-ssh).

```groovy
// Gradle
compile 'org.hidetake:groovy-ssh:x.y.z'
```

Instantiate a [Service](src/main/groovy/org/hidetake/groovy/ssh/core/Service.groovy) by [`Ssh#newService()`](src/main/groovy/org/hidetake/groovy/ssh/Ssh.groovy)
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
    put 'example.war', '/webapps'
    execute 'sudo service tomcat restart'
  }
}
```

Contributions
-------------

This is an open source software licensed under the Apache License Version 2.0.
Send me your issue or pull request.
