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
The project contains Gradle wrapper and Gradle installation is not needed.


### Add the plugin dependency

Add Gradle SSH plugin to the script:

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

The plugin is available on Maven Central repository.
Gradle will fetch the plugin from Internet.


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


### Declare a SSH session

There are two ways to use SSH facility.

1. Create a SSH task
2. Call SSH method in the task


#### Create a SSH task

This may be mostly used.
The plugin provides type of a SSH task as `SshTask`.
It is a generic Gradle task and provides trivial features such as `dependsOn: task`, `doFirst` or `doLast`.

Following code creates a SSH task in the script:

```groovy
task checkWebServer(type: SshTask) {
  session(remotes.web01) {
    //execute ...
  }
  session(remotes.web02) {
    //execute ...
  }
}
```

Invoke Gradle with name of the task to execute it.

```bash
./gradlew checkWebServer
```

The SSH task will connect to all remote hosts of sessions, i.e. web01 and web02.
And it will evaluate each closure of sessions in order.


#### Call sshexec method in a task

The plugin also supports calling `sshexec` in a task.
This may be useful if more complex scenario is needed.

Exactly same syntax is available in a SSH task and the sshexec method,
but the sshexec method returns a value of the last session closure.

Here is an example.

```groovy
task syncKernelParam << {
  def paramKey = 'net.core.wmem_max'
  def paramValue = sshexec {
    session(remotes.web01) {
      execute("sysctl '$paramKey' | sed -e 's/ //g'")
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
session(remotes.web01, remotes.web02) {
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


Manage remote hosts
-------------------

The plugin adds a container of remote hosts to the project.
The remote hosts container is an [NamedDomainObjectContainer](http://www.gradle.org/docs/current/javadoc/org/gradle/api/NamedDomainObjectContainer.html) and has role support methods extended by the plugin.


### Add a remote host

Following code adds a remote host to the remote hosts container:

```groovy
remotes {
  web01 {
    host = '192.168.1.101'
    user = 'jenkins'
  }
}
```

Following settings can be set in a remote closure.

Key       | Type              | Description
----------|-------------------|------------
`host`    | String, Mandatory | Hostname or IP address.
`port`    | Integer           | Port. Default is 22.
`gateway` | Remote            | Gateway remote host. If this is set, port-forwarding tunnel will be used on connection.
`proxy`   | Proxy             | Proxy server. If this is set, the connection will use the proxy server to reach the remote host.


#### Set connection settings

Also following settings can be set in a remote closure. These can be set globally in the project.

Key            | Type              | Description
---------------|-------------------|------------
`user`         | String, Mandatory | User name.
`password`     | String            | A password for password authentication.
`identity`     | File              | A private key file for public-key authentication.
`passphrase`   | String            | A pass-phrase of the private key. This can be null.
`agent`        | Boolean           | If this is set, Putty Agent or ssh-agent will be used on authentication.
`knownHosts`   | File              | A known hosts file. Default is `~/.ssh/known_hosts`. If `allowAnyHosts` is set, strict host key checking is turned off (only for testing purpose).
`retryCount`   | Integer           | Retry count to establish connection. Default is 0 (no retry).
`retryWaitSec` | Integer (seconds) | Interval time between each retries. Default is 0 (immediately).


#### Connect through gateway servers

A remote host can be connected through one or more gateway servers.

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


#### Connect through a proxy server

A remote host can specify that connections should be made through a proxy server. Individual proxy server connections are configured in the `proxies` container provided by the plugin. 

The following code adds a proxy server to the `proxies` container:

```groovy
proxies {
  socks01 {
    host = '192.168.1.112'
    port = 1080
    type = SOCKS
  }
}
```

The following settings are used to configure how a proxied connection is established within a proxy closure.

Key            | Type                 | Description
---------------|----------------------|------------
`host`         | String, Mandatory    | Hostname or IP address.
`port`         | Integer, Mandatory   | Port.
`type`         | ProxyType, Mandatory | Type of proxy server: `SOCKS`or `HTTP`. 
`user`         | String               | Proxy server user name.
`password`     | String               | Proxy server password.
`socksVersion` | Integer              | Protocol version when using `SOCKS`: 4 or 5. Defaults to 5.

Once a proxy server is defined in the `proxies` container, it can be referenced per-remote, per-task or globally. Unless the remote's proxy property is set in a higher scope, connections made to that host will not be proxied. 

The following code shows how remote hosts can use different proxy servers.

```groovy
proxies {
  socks {
    host = '192.168.1.112'
    port = 1080
    user = 'admin'
    password = '0t1s'
    type = SOCKS
    socksVersion = 5
  }
  
  http {
    host = '192.168.1.113'
    port = 8080
    type = HTTP
  }
}

remotes {
  web01 {
    host = '192.168.1.101'
    user = 'jenkins'
    proxy = proxies.http
  }
  
  web02 {
    host = '192.168.1.102'
    user = 'jenkins'
    proxy = proxies.socks
  }
}
```

The following shows how to set a global proxy server.

```groovy
ssh {
  // All remotes will use this proxy by default.
  // Each remote can override this configuration.
  proxy = proxies.socks01
}
```

The following shows how to set a proxy server on a particular task.

```groovy
task jarSearch(type: SshTask) {
  ssh {
    proxy = proxies.http01
  }
  session(remotes.role('mavenRepo')) { ... }
}
```


### Associate with roles

Call `role` method to associate the host with one or more roles.

```groovy
remotes {
  web01 {
    role('webServers')
    role('all')
    host = '192.168.1.101'
    user = 'jenkins'
  }
}
```

We can specify one or mote roles on a session.

```groovy
session(remotes.role('all')) {
  //execute ...
}

session(remotes.role('webServer', 'appServer')) {
  //execute ...
}
```


### Manipulate on execution phase

A remote host can be defined on execution phase by `remotes.create(name)`.

```groovy
task setupRemote << {
  sshexec {
    session(remotes.web01) {
      def targetHost = execute 'cat settings/hostname'
      def targetUser = execute 'cat settings/username'
      // Define a remote host dynamically
      remotes.create('dynamic1') {
        host = targetHost
        user = targetUser
      }
    }
  }
}

task something(dependsOn: setupRemote) << {
  sshexec {
    session(remotes.dynamic1) {
      //execute ...
    }
  }
}
```


Perform operations
------------------

Following methods are available in a session closure.

* `execute` - Execute a command.
* `executeBackground` - Execute a command in background.
* `executeSudo` - Execute a command with sudo support.
* `shell` - Execute a shell.
* `put` - Put a file or directory into the remote host.
* `get` - Get a file or directory from the remote host.


### Execute a command

Call the `execute` method with a command to execute.

```groovy
execute 'sudo service httpd reload'
```

The method can be called with operation settings.

```groovy
execute 'sudo service httpd reload', pty: true
```

The method waits until the command is completed and returns a result from standard output of the command.
Line separators are converted to the platform native.

```groovy
def result = execute 'uname -a'
println result
```

A result can be retrieved as an argument if a closure is given.

```groovy
execute('uname -a') { result ->
  println result
}
```

The method throws an exception if an exit status of the command was not zero.


### Execute a command in background

Call the `executeBackground` method with a command to execute in background.

```groovy
executeBackground 'sudo service httpd reload'

// also can be called with operation settings
executeBackground 'sudo service httpd reload', pty: true
```

The method does not wait for the command.
Other commands are executed concurrently.

```groovy
// httpd processes on all web servers will be reloaded concurrently
session(remotes.role('webServers')) {
  executeBackground 'sudo service httpd reload'
}

// ping to many hosts concurrently
session(remotes.web01) {
  (1..127).each { lastOctet ->
    executeBackground "ping -c 1 -w 1 192.168.1.$lastOctet"
  }
}
```

A result can be retrieved as an argument if a closure is given.

```groovy
executeBackground('ping -c 3 server') { result ->
  def average = result.find('min/avg/.+=.+?/.+?/').split('/')[-1]
}
```

The method throws an exception if an exit status of the command is not zero.
If a background command returned an error, the task or sshexec method waits for any other commands and throws an exception finally.


### Execute a command with the sudo support

Call the `executeSudo` method with a command to execute with the sudo support.
The method prepends `sudo -S -p` to the command and will provide a password for sudo prompt.

```groovy
executeSudo 'service httpd reload'

// also can be called with operation settings
executeSudo 'service httpd reload', pty: true
```

The method waits until the command is completed and returns a result from standard output of the command, excluding sudo interactions.
Line separators are converted to the platform native.

```groovy
def result = executeSudo 'service httpd status'
println result
```

A result can be retrieved as an argument if a closure is given.

```groovy
executeSudo('service httpd status') { result ->
  println result
}
```

The method throws an exception if an exit status of the command was not zero, including the sudo authentication failure.

The sudo support is achieved by the stream interaction support. So the method does not accept an `interaction` setting.


### Execute a shell

Call the `shell` method to execute a shell.
The method is useful for a limited environment which supports only a shell such as Cisco IOS.

A stream interaction setting should be given in order to exit the shell.

```groovy
session(remotes.web01) {
  shell interaction: {
    when(partial: ~/.*$/) {
      standardInput << 'exit 0' << '\n'
    }
  }
}
```

The method throws an exception if an exit status of the shell was not zero.


### Transfer a file or directory

Call the `get` method to get a file or directory from the remote host.

```groovy
get '/remote/file', 'local_file'

// also accepts a File object
get '/remote/file', buildDir
```

Call the `put` method to put a file or directory into the remote host.

```groovy
put 'local_file', '/remote/file'

// also accepts a File object
put buildDir, '/remote/folder'

// also accepts an Iterable<File>
put files('local_file1', 'local_file2'), '/remote/folder'
```

The method throws an exception if an error occurred while the file transfer.


### Operation settings

Following settings can be given to operation methods.

Key              | Type     | Description
-----------------|----------|------------
`dryRun`         | Boolean  | Dry run flag. If this is true, no action is performed. Default is false.
`pty`            | Boolean  | If this is true, the PTY allocation is requested on the command execution. Default is false.
`logging`        | Boolean  | If this is false, the logging of standard output and error is turned off, for such as hiding credential. Default is true.
`outputLogLevel` | LogLevel | Log level of the standard output on the command or shell execution. Default is `LogLevel.QUIET`.
`errorLogLevel`  | LogLevel | Log level of the standard error on the command or shell execution. Default is `LogLevel.ERROR`.
`encoding`       | String   | Encoding of input and output on the command or shell execution. Default is `UTF-8`.
`interaction`    | Closure  | Specifies an interaction with the stream on the command or shell execution. Default is no interaction.
`extensions`     | List of classes | List of extension classes. If this is set, classes will be mixed in.


### The stream interaction support

The execute method can interact with the stream of command executed on the remote host.
The shell method can do same.
This feature is useful for providing a password or yes/no answer.


#### Declare interaction rules

Call the execute or shell method with an `interaction` setting which contains one or more interaction rules.
Interaction rules will be evaluated in order.
If any rule has been matched, others are not evaluated more.

The following example declares 2 rules.

```groovy
interaction: {
  // Declare a rule
  when(/* a pattern match */) {
    /* an action closure */
  }

  // Below rule will be evaluated only if above is not matched
  when(/* a pattern match */) {
    /* an action closure */
  }
}
```


#### An interaction rule is

An interaction rule consists of a pattern match and an action closure.
The action closure will be executed if the pattern match is satisfied.

A pattern match is one of the following.

* `when(partial: pattern, from: stream)`
  Declares if a partial string from the stream is matched to the pattern.
* `when(line: pattern, from: stream)`
  Declares if a line from the stream is matched to the pattern.
* `when(nextLine: pattern, from: stream)`
  Declares if an next line from the stream is matched to the pattern.

`partial` is evaluated when the stream is flushed.
But `line` and `nextLine` is evaluated when the stream gives a line separator.

The pattern is one of the following.

* If the pattern is a string, it performs exact match.
* If the pattern is a regular expression, it performs regular expression match. Groovy provides pretty notation such as `~/pattern/`.
* If the pattern is `_`, it matches to any line even if empty.

The stream is one of the following.

* `standardOutput` - Standard output of the command.
* `standardError` - Standard error of the command.
* If the stream is omitted, it means any.

Now explaining another one of an interaction rule, an action closure.

An action closure is a generic Groovy closure executed if the pattern match is satisfied.
It can write a string to the `standardInput`.

```groovy
interaction: {
  when(partial: ~/.*#/) {
    standardInput << 'exit' << '\n'
  }
}
```

If an action closure contains one or more interaction rules, surrounding rules are discarded and inner rules are activated.
In the following case, at first, A and B are evaluated for an each line or partial string,
but C is evaluated after A has been matched.

```groovy
interaction: {
  when(/* rule A */) {
    when(/* rule C */) {
    }
  }
  when(/* rule B */) {
  }
}
```


#### Example: handle the prompt

Let's take a look at the following example.

```groovy
// Execute a shell with the interaction support
shell interaction: {
  // Declare a rule if the stream gives a string terminated with $
  when(partial: ~/.*$/) {
    // If the rule is matched, provides the exit to the shell
    standardInput << 'exit 0' << '\n'
  }
}
```

The example will execute a shell and provide the exit if the prompt appears.

If the shell prompt is `sh$`, pattern matching will work as follows.

1. The stream gives `s` and the line buffer becomes `s`.
2. The pattern match is evaluated but not matched.
3. The stream gives `h` and the line buffer becomes `sh`.
4. The pattern match is evaluated but not matched.
5. The stream gives `$` and the line buffer becomes `sh$`..
6. The pattern match is evaluated and matched. The closure is executed.


#### Example: handle more prompts

TODO

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

