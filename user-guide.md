---
layout: page
title: User Guide
---


Overview
--------

Gradle SSH Plugin is a Gradle plugin which provides remote execution and file transfer features.


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
This project contains Gradle wrapper so Gradle installation is not needed.


### Add the plugin dependency

Add the plugin dependency to the script:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:{{ site.product.version }}'
  }
}

apply plugin: 'ssh'
```

The plugin is available on Maven Central repository so no installation is needed.
Gradle will fetch the plugin from Internet.


### Add a remote host

The plugin adds a container of remote hosts to the project.
One or more remote hosts can be added in the `remotes` closure.

Following code adds a remote host to the remote hosts container:

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

Now we can access the remote host by `remotes.web01` or `remotes.web02`.

A remote host can be associated with one or more roles.
See below section for details.


### Create a task

There are two ways to use SSH facility.

1. Create a SSH task and invoke Gradle with it
2. Call SSH method in the task


#### Create a SSH task

This may be main usage.
The plugin provides type of a SSH task as `SshTask`.
It is a Gradle task so provides general features such as `dependsOn: task`, `doFirst` or `doLast`.

Create a SSH task in the script:

```groovy
task checkWebServer(type: SshTask) {
  session(remotes.web01) {
    def pids = execute('pidof nginx').split(/ /)
    assert pids.length > 1
  }
}
```

Invoke Gradle with name of the task.

```bash
./gradlew checkWebServer
```

Gradle will execute the task.
The SSH task will connect to remote hosts declared on `session` and will evaluate each closure in order.


#### Call SSH method in the task

Also calling SSH in the task is supported.
This may be useful when more complex scenario is needed.

```groovy
task syncKernelParams << {
  def paramKey = 'net.core.wmem_max'
  def paramValue = null
  sshexec {
    session(remotes.web01) {
      paramValue = execute("sysctl '$paramKey' | sed -e 's/ //g'")
    }
  }
  assert paramValue.contains(paramKey)
  sshexec {
    session(remotes.web02) {
      execute("sysctl -w '$paramValue'")
    }
  }
}
```

There is no functional difference between creating a SSH task and calling SSH method.
Exactly same syntax is available on both cases.


### Describe SSH operations

Now describe SSH operations in the SSH task or method.


#### Declare a session

A session consists of a remote host to connect and a closure.
Following code declares a session which connects to the remote host `web01` and executes a command.

```groovy
session(remotes.web01) {
  execute 'sudo service httpd reload'
}
```

If more than one remote hosts are given, the plugin will connect to the remote host and execute the closure in order.
For instance,

```groovy
session(remotes.web01, remotes.web02) {
  execute 'sudo service httpd reload'
}
```

is equivalent to:

```groovy
session(remotes.web01) {
  execute 'sudo service httpd reload'
}
session(remotes.web02) {
  execute 'sudo service httpd reload'
}
```


#### Describe operations in the closure

Following methods are available to perform operations. See below section for details.

* `execute`
* `executeBackground`
* `executeSudo`
* `shell`
* `put`
* `get`


Manage remote hosts
-------------------

The plugin adds a container of remote hosts to the project.
It is an [NamedDomainObjectContainer](http://www.gradle.org/docs/current/javadoc/org/gradle/api/NamedDomainObjectContainer.html) and has role support methods extended by the plugin.


### Add a remote host

```groovy
remotes {
  web01 {
  }
}
```

Following settings can be set in a remote closure.

* `host` - Hostname or IP address. (Mandatory)
* `port` - Port. Default is 22.
* `gateway` - Gateway remote host. If this is set, port-forwarding tunnel will be used to connect.


#### Set connection settings

Also following settings can be set in a remote closure. These can be set globally.

* `user` - User name. (Mandatory)
* `password` - Password for password authentication.
* `identity` - Private key file for public-key authentication. This overrides global identity.
* `passphrase` - Pass phrase for the private key.
* `agent` - If this flag is set, Putty Agent or ssh-agent will be used to authentication.
* `knownHosts` - Known hosts file. Default is `~/.ssh/known_hosts`. If `allowAnyHosts` is set, strict host key checking is turned off (only for testing purpose).
* `retryCount` - Retrying count to establish connection. Default is 0 (no retry).
* `retryWaitSec` - Time in seconds between each retries. Default is 0 (immediately).


#### Connect through gateway servers

A remote host can be accessed through one or more gateway servers.

```groovy
remotes {
  gw01 {
    host = '10.2.3.4'
    user = 'gwuser'
  }
  web01 {
    host = '192.168.1.101'
    user = 'jenkins'
    gateway = remotes.gw01
  }
}
```


### Access remote hosts in the container

Use `role` method to associate the host with roles. A remote host can be associated with multiple roles.

* `remotes.name` - Specifies the remote host.
* `remotes.role(A)` - Specifies remote hosts associated with A.
* `remotes.role(A, B)` - Specifies remote hosts associated with A _or_ B.


### Manipulate on execution phase

A remote host can be defined dynamically by `remotes.create(name)`.

```groovy
remotes.create('dynamic1') {
  host = /* given in execution phase */
  user = /* given in execution phase */
}

task something << {
  sshexec {
    session(remotes.dynamic1) {
      execute('...')
    }
  }
}
```


Perform operations
------------------

Following operations are available in the session.

  * Command execution
  * Shell execution
  * File transfer


### Execute a command

Use following methods to execute a command in the session closure.

* `execute(command)` - Executes a command. This method blocks until the command is completed and returns output of the command.
* `executeSudo(command)` - Executes a command as sudo (prepends sudo -S -p). Used to support sudo commands requiring password. This method blocks until the command is completed and returns output of the command.
* `executeBackground(command)` - Executes a command in background. Other operations will be performed concurrently.


#### Handle the result

Following methods return value:

* `execute` returns a string from standard output of the remote command. Line separators are converted to platform native.
* `executeSudo` returns a string from standard output of the remote command, excluding sudo interactions. Line separators are same as above.

Also `execute`, `executeBackground` and `executeSudo` can take a callback closure _(since v0.3.1)_.
It will be called with the result when the command is finished.

```groovy
executeBackground('ping -c 3 server') { result ->
  def average = result.find('min/avg/.+=.+?/.+?/').split('/')[-1]
}
```


#### Handle the error

These methods raise an exception and stop Gradle if error occurs:

* `execute` throws an exception if exit status of the remote command is not zero.
* `executeSudo` throws an exception if exit status of the remote command is not zero, including sudo authentication failure.
* `executeBackground` throws an exception if exit status of the remote command is not zero, but does not interrupt any other background operations. If any command cause error, the task will be failed.


### Execute a shell

Use `shell` method to execute a shell in the session closure.
This method blocks until the shell is finished and will throw an exception if exit status of the shell is not zero.
Stream interaction setting should be given in order to exit the shell.


### Transfer a file or directory

Use following methods to transfer files in the session closure.

* `get(remote, local)` - Fetches a file or directory from remote host.
* `put(local, remote)` - Sends a file or directory to remote host.

These methods raise an exception and stop Gradle if error occurs.

It is strongly recommended to pack files into a archive and transfer it for performance reason.


### Set operation settings

Following settings can be passed to operation methods.

* `dryRun` - Dry run flag. If true, performs no action. Default is false.
* `pty` - Requests PTY allocation if true. Default is false. Only valid for command execution.
* `logging` -  Turns off logging of standard output and error if false. e.g. hiding credential. Default is true.
* `outputLogLevel` - Log level of standard output while command execution. Default is `LogLevel.QUIET`.
* `errorLogLevel` - Log level of standard error while command execution. Default is `LogLevel.ERROR`.
* `encoding` - Encoding of input and output for executing commands. Default is UTF-8.
* `interaction` - Specifies interaction with the stream _(since v0.3.1)_. Default is no interaction.
* `extensions` - List of extension classes. If given, classes will be mixin on session execution.


Declare stream interaction
--------------------------

`execute`, `executeBackground` and `shell` can take a setting for interaction with the stream.

```groovy
execute('passwd', pty: true, interaction: {
  when(partial: ~/.+[Pp]assowrd: */) {
    standardInput << oldPassword << '\n'
    when(partial: ~/.+[Pp]assowrd: */) {
      standardInput << newPassword << '\n'
    }
  }
})
```

We can write a string to the `standardInput` to interact with the command.


### Declare interaction rules

Use following methods to declare rules in the interaction closure.

* `when(nextLine: pattern, from: stream) { action }` - When next line from the `stream` matches to the `pattern`, the action will be called.
* `when(line: pattern, from: stream) { action }` - When a line from the `stream` matches to the `pattern`, the action will be called.
* `when(partial: pattern, from: stream) { action }` - Performs match when the stream is flushed. This is useful for answering to a prompt such as `yes or no?`.

`pattern` is one of following:

* If pattern is a string, it performs exact match.
* If pattern is a regular expression, it performs regular expression match. Groovy provides pretty notation such as `~/pattern/`.
* If pattern is `_`, it matches to any line even if empty.

`stream` is one of following:

* `standardOutput` - Standard output of the command.
* `standardError` - Standard error of the command.
* If stream is omitted, it means any.

Rules will be evaluated in order. First rule has the highest priority.


Override settings
-----------------

Connection settings and operation settings can be set globally
and overridden by each remote hosts, tasks or operation methods.


Category            | Global | Per task | Per remote | Per operation
--------------------|--------|----------|------------|--------------
Connection settings | x      | x        | x          | -
Operation settings  | x      | x        | -          | x


Connection settings and operation settings can be set globally in the ssh closure.

```groovy
ssh {
  knownHosts = allowAnyHosts
  dryRun = true
}
```

Connection settings and operation settings can be overridden in a task.

```groovy
task reloadServers(type: SshTask) {
  ssh {
    pty = true
  }
  session(remotes.role('webServers')) {
    executeBackground('sudo service httpd reload')
  }
}
```

Same in a sshexec closure.

```groovy
sshexec {
  ssh {
    pty = true
  }
  session(remotes.role('webServers')) {
    executeBackground('sudo service httpd reload')
  }
}
```

Connection settings can be overridden in a remote host closure.

```groovy
remotes {
  web01 {
    host = '192.168.1.101'
    user = 'jenkins'
    identity = file('id_rsa_jenkins')
  }
}
```

Operation settings can be overridden on an operation method.

```groovy
execute('sudo service httpd reload', pty: false)
execute('sudo service httpd reload', logging: false)
```


Add custom DSL
--------------

We can extend DSL syntax.

Declare an extension class and add it to global or task specific settings.
All methods in the class will be available in the session closure.

```groovy
class RemoteFileAssertion {
  def assertFileContains(String path, String regexp) {
    execute("egrep '$regexp' '$path'")
  }
}

ssh {
  extensions.add RemoteFileAssertion
}

task checkApacheConfig(type: SshTask) {
  session(remotes.webServer) {
    assertFileContains '/etc/httpd/conf/httpd.conf', 'listen 80'
  }
}
```

