byteatebit-nbecho
=================
byteatebit-nbecho is a simple echo server built to demonstrate the byteatebit-nbserver project.  To build the byteatebit-nbecho project
you will first need to download and install its dependencies:

1. [byteatebit-common](https://github.com/byteatebit/byteatebit-common "byteatebit-common") `./gradlew install`
+  [byteatebit-nbserver](https://github.com/byteatebit/byteatebit-nbserver "byteatebit-nbserver") `./gradlew install`

The byteatebit-nbecho service listens on a configurable port and echos back to the client each line of input provided.

```
usage: NbEchoServer
 -h,--help                      Display help
 -it,--io-threads <arg>         Number of IO threads for the server.  The
                                default value is 2
 -p,--port <arg>                Listen port
 -st,--selector-timeout <arg>   The amount of time to wait on a select
                                call.  The default is 1 ms.  A value of 0
                                results in a selectNow() call with a
                                thread yield on 0 ready keys.
 -t,--threads <arg>             Number of threads for the server.  The
                                minimum value is 2, one for IOTasks and
                                one for the ComputeTask executor
```

Run `./gradlew build` from the project directory to build the project, run the unit tests, and assemble the nbecho deployment package.
There are 3 possible ways to run the applications in the nbecho project.

1.  Run the scripts from the project build directory, eg `build/byteatebit-nbecho/scripts/echo-server.sh`.  Your
JAVA_HOME point at a Java 8 JVM.
+  Install the debian package in the  build/distributions directory on a linux machine with
  `dpkg -i byteatebit-nbecho_0.1.0-1_all.deb`.  You will first need to install a Java 8 JRE on the machine and set your JAVA_HOME
  accordingly.
+  Create a docker image using the supplied Dockerfile.  This procedure assumes you have docker running on a host
   and have your shell environment setup appropriately, eg `eval $(docker-machine env default)`.
    + `cd build/distributions`
    + `docker build -t byteatebit-nbecho:latest .`
    + `docker run --rm -ti --net=host byteatebit-nbecho`
    + To connect, identify your docker host IP via `echo ${DOCKER_HOST}`.

Once the server is up and running, you can use telnet to verify eg.

```
$telnet 192.168.99.102 8080
telnet: Warning: -x ignored, no ENCRYPT support.
Trying 192.168.99.102...
Connected to 192.168.99.102.
Escape character is '^]'.
echo this
echo this
quit
Connection closed by foreign host.
```

