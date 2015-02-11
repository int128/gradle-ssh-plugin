---
layout: page
title: Getting Started
---


Requirement
-----------

Gradle SSH Plugin requires following:

* Java 6 or later
* Gradle 2.0 or later


Getting Started
---------------

1. Create a script
2. Add the plugin dependency
3. Add a remote host
4. Create a task
5. Describe SSH operations in the task


### Create a script

Create an empty file and save as `build.gradle`.

If you do not have installed Gradle, get the [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template) for quick start.
The project contains Gradle wrapper and Gradle installation is not needed.


### Add the plugin dependency

The plugin is available on Bintray JCenter repository.
Gradle will fetch the plugin from Internet.

Add the plugin to your script:

```groovy
plugins {
  id 'org.hidetake.ssh' version '{{ site.product.version }}'
}
```

Gradle 2.0 style:

```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:{{ site.product.version }}'
  }
}

apply plugin: 'org.hidetake.ssh'
```


### Add a remote host

The plugin adds a container of remote hosts to the project.
One or more remote hosts can be added in the `remotes` closure.
A remote host can be associated with one or more roles.

Following code adds remote hosts to the remote hosts container:

```groovy
remotes {
  web01 {
    role 'masterNode'
    host = '192.168.1.101'
    user = 'jenkins'
  }
  web02 {
    host = '192.168.1.102'
    user = 'jenkins'
  }
}
```

Now we can specify each remote host by `remotes.web01` or `remotes.web02`.
Also we can specify the web01 by `remotes.role('masterNode')`.


### Run a SSH session in the task

Following code run a SSH session in the script:

```groovy
task checkWebServer << {
  ssh.run {
    session(remotes.web01) {
      //execute ...
    }
    session(remotes.web02) {
      //execute ...
    }
  }
}
```

`ssh.run` method will connect to all remote hosts of sessions, i.e. web01 and web02,
and evaluate each closure of sessions in order.

More example.

```groovy
task syncKernelParam << {
  def paramKey = 'net.core.wmem_max'
  def paramValue = ssh.run {
    session(remotes.web01) {
      execute("sysctl '$paramKey' | sed -e 's/ //g'")
    }
  }
  assert paramValue.contains(paramKey)
  ssh.run {
    session(remotes.web02) {
      execute("sysctl -w '$paramValue'")
    }
  }
}
```


#### More sessions

A session consists of a remote host to connect and a closure.
Following code declares a session which connects to web01 and executes a command.

```groovy
session(remotes.web01) {
  //execute ...
}
```

If more than one remote hosts are given, the plugin will connect to all remote hosts at once and execute closures in order.
For instance,

```groovy
session([remotes.web01, remotes.web02]) {
  //execute ...
}
```

is equivalent to:

```groovy
session(remotes.web01) {
  //execute ...
}
session(remotes.web02) {
  //execute ...
}
```

Also the session method accepts properties of a remote host without having to declare it on the remote host container.

```groovy
session(host: '192.168.1.101', user: 'jenkins') {
  //execute ...
}
```


### Describe SSH operations

Now describe SSH operations in the session closure.
SSH operation methods and any Groovy or Gradle methods can be used.

```groovy
session(remotes.web01) {
  // Execute a command
  def result = execute 'uptime'

  // Any Gradle methods or properties can be used in a session closure
  copy {
    from "src/main/resources/example"
    into "$buildDir/tmp"
  }

  // Also Groovy methods or properties can be used in a session closure
  println result
}
```

Following operations are available. See later section for details.

* Command execution
* Shell execution
* File transfer
