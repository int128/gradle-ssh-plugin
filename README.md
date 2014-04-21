Gradle SSH Plugin [![Build Status](https://travis-ci.org/int128/gradle-ssh-plugin.svg?branch=master)](https://travis-ci.org/int128/gradle-ssh-plugin)
=================

Gradle SSH Plugin provides remote command execution and file transfer on Gradle.


Features
--------

### Integrated with Gradle

Seamlessly integrated with Gradle DSL.


### Remote command execution

```groovy
task reloadServers(type: SshTask) {
  session(remotes.role('webServers')) {
    execute 'sudo service httpd reload'
  }
}
```


### File transfer

```groovy
task deployApp(type: SshTask) {
  session(remotes.role('webServers')) {
    put("$buildDir/libs/hello.war", '/webapps')
    execute('sudo service tomcat restart')
  }
}
```


### Authentication and security

* Password authentication
* Public key authentication
* Host key verification with an OpenSSH compatible `known_hosts` file


### Stream interaction

* Providing a password for sudo prompt
* Interaction with shell such as bash or Cisco IOS


How to use
----------

Add the plugin dependency into your build.gradle:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'org.hidetake:gradle-ssh-plugin:0.3.3'
  }
}

apply plugin: 'ssh'
```

Use [Gradle SSH Plugin Template Project](https://github.com/gradle-ssh-plugin/template) for quick start.


Define a remote host
--------------------

At first, define remote hosts:

```groovy
remotes {
  web01 {
    role('masterNode')
    host = '192.168.1.101'
    user = 'jenkins'
  }
  web02 {
    host = '192.168.1.102'
    user = 'jenkins'
  }
}
```

Following settings can be set in a remote closure.
  * `host` - Hostname or IP address. (Mandatory)
  * `port` - Port. Default is 22.
  * `gateway` - Gateway remote host. If this is set, port-forwarding tunnel will be used to connect.

Use `role` method to associate the host with roles. A remote host can be associated with multiple roles.


### Connection settings

Also following settings are available in a remote closure.
These settings can be set globally, see [global settings section](#global-settings).
  * `user` - User name. (Mandatory)
  * `password` - Password for password authentication.
  * `identity` - Private key file for public-key authentication. This overrides global identity.
  * `passphrase` - Pass phrase for the private key.
  * `agent` - If this flag is set, Putty Agent or ssh-agent will be used to authentication.
  * `knownHosts` - Known hosts file. Default is `~/.ssh/known_hosts`. If `allowAnyHosts` is set, strict host key checking is turned off (only for testing purpose).
  * `retryCount` - Retrying count to establish connection. Default is 0 (no retry).
  * `retryWaitSec` - Time in seconds between each retries. Default is 0 (immediately).


### Access through gateway servers

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


Define a SSH task
-----------------

Define a SSH task to execute commands or transfer files.

```groovy
task checkWebServer(type: SshTask) {
  session(remotes.web01) {
    def pids = execute('pidof nginx').split(/ /)
    assert pids.length > 1
  }
}

task reloadServers(type: SshTask) {
  session(remotes.role('webServers', 'appServers')) {
    executeBackground('sudo service httpd reload', pty: true)
  }
}
```

The plugin runs task closures in **evaluation** phase of Gradle, but will run each session closure in **execution** phase of Gradle.


### Open a session

In a task closure, use following methods to open a session:
  * `session(remote)` - Adds a session to the remote host.
  * `session(remotes)` - Adds each session of remote hosts. If a list is given, sessions will be executed in order. Otherwise, order is not defined.


#### Specify a remote host by name or role

A session method takes one or more remote hosts.
  * `remotes.name` - Specifies the remote host.
  * `remotes.role(A)` - Specifies remote hosts associated with A.
  * `remotes.role(A, B)` - Specifies remote hosts associated with A _or_ B.


### Execute a command or shell

In a session closure, use following methods to execute a command or shell:
  * `execute(command)` - Executes a command. This method blocks until the command is completed and returns output of the command.
  * `executeSudo(command)` - Executes a command as sudo (prepends sudo -S -p). Used to support sudo commands requiring password. This method blocks until the command is completed and returns output of the command.
  * `executeBackground(command)` - Executes a command in background. Other operations will be performed concurrently.
  * `shell` - Opens a shell. This method blocks until the shell is finished. You should provide interaction such as `exit` or `quit` in order to exit the shell.

Also following property is available:
  * `remote` - Remote host of current session. (Read only)


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
  * `shell` throws an exception if exit status of the shell is not zero.


#### Interact with the stream

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

In an interaction closure, use following methods to declare rules:
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

In an action closure, following property is available:
  * `standardInput` - Output stream to the remote command. (Read only)


### Transfer a file or directory

In a session closure, use following methods to transfer files:
  * `get(remote, local)` - Fetches a file or directory from remote host.
  * `put(local, remote)` - Sends a file or directory to remote host.

It is strongly recommended to pack files into a archive and transfer it for performance reason.


#### Handle the error

These methods raise an exception and stop Gradle if error occurs.


### Operation settings

Following settings can be passed to operation methods such as `execute` or `shell`.
  * `dryRun` - Dry run flag. If true, performs no action. Default is false.
  * `pty` - Requests PTY allocation if true. Default is false. Only valid for command execution.
  * `logging` -  Turns off logging of standard output and error if false. e.g. hiding credential. Default is true.
  * `outputLogLevel` - Log level of standard output while command execution. Default is `LogLevel.QUIET`.
  * `errorLogLevel` - Log level of standard error while command execution. Default is `LogLevel.ERROR`.
  * `encoding` - Encoding of input and output for executing commands. Default is UTF-8.
  * `interaction` - Specifies interaction with the stream _(since v0.3.1)_. Default is no interaction.


Use SSH in the task
-------------------

`sshexec` method provides SSH execution in another task.
In `sshexec` closure, same properties and methods as `SshTask` are available.

```groovy
task reloadService << {
  def serviceName = /* given in execution phase */
  sshexec {
    session(remotes.server01) {
      execute("sudo service $serviceName reload")
    }
  }
}
```


### Manipulate the remotes container

Since `remotes` is a [NamedDomainObjectContainer](http://www.gradle.org/docs/current/javadoc/org/gradle/api/NamedDomainObjectContainer.html),
a remote host can be defined dynamically using `remotes.create(name)`:

```groovy
remotes.create('dynamic1') {
  host = /* given in execution phase */
  user = /* given in execution phase */
}
sshexec {
  session(remotes.dynamic1) {
    execute('...')
  }
}
```


Global settings
---------------

[Connection settings](#connection-settings) and [operation settings](#operation-settings)
can be set globally and overridden by each remote hosts, tasks or operation methods.


Category            | Global | Per task | Per remote | Per operation
--------------------|--------|----------|------------|--------------
Connection settings | x      | x        | x          | -
Operation settings  | x      | x        | -          | x


Connection settings and operation settings can be set globally in the `ssh` closure.
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

Same in a `sshexec` closure.
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


Contributions
-------------

Send your issue or pull request to [GitHub repository](https://github.com/int128/gradle-ssh-plugin).
