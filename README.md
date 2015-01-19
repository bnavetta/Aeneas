## Aeneas

Aeneas is a dynamic, distributed cloud orchestration system. Its goal is to make
scaling and configuring a service as simple as running an instance on a new
node.

The primary focus will be on foundational services like
[ZooKeeper](http://zookeeper.apache.org/), [Mesos](http://mesos.apache.org/),
[Kubernetes](https://github.com/GoogleCloudPlatform/kubernetes), or
[Marathon](https://github.com/mesosphere/marathon). Aeneas will handle scaling
and connecting these systems so that applications run on then can be distributed
across a cluster. For example, additional Kubernetes nodes or Mesos slaves are
automatically registered with the appropriate scheduler or master, which will
begin to utilize the new resources.

Aeneas requires a distributed storage mechanism for coordination and
configuration. Rather than reinvent the wheel,
[etcd](https://github.com/coreos/etcd) is being used. In
the future, other stores may be used as well.

### ZooKeeper Support

Aeneas builds a custom [`zookeeper-node`](zookeeper/zk-node) image that reads cluster members
from the shared configuration store (via [`zk-common`](zookeeper/zk-common)) and configuration
from the environment. Nodes retain their identity if stopped and restarted or obtain a new id
if necessary. The image also registers and deregisters itself automatically.

The [`zookeeper-manager`](zookeeper/zk-manager) image watches for new nodes to register or deregister
themselves and updates the quorum. New nodes will automatically be added as soon as they are ready, and
old ones are removed as they shut down. As long as at least three nodes are running, the quorum should
survive any changes in membership, including leader death.

### Command Line Interface

The [`aeneas-cli`](aeneas-cli) project is a basic utility for managing assorted
containers and services. It's probably not quite sufficient for heavy use, but
at least provides a decent starting point. In further examples, the `aeneas` command
refers to the `aeneas-cli-capsule.jar` executable JAR. 

#### Example: Start a ZooKeeper cluster

```shell
$ aeneas zookeeper create 1 2 3 4 aeneas-zookeeper-5=2777;3777;2177

$ aeneas zookeeper start 1

# wait a few seconds
$ aeneas zookeeper start 2

# wait a few seconds
$ aeneas zookeeper start aeneas-zookeeper-3

# wait a few seconds
$ aeneas zookeeper manager -d

$ aeneas zookeeper start 4 5

$ aeneas zookeeper list
# should see a list of 5 nodes

# some time later...

$ aeneas zookeeper stop 1 2 3 4 5
$ aeneas zookeeper remove 1 2 3 4 5
$ docker stop aeneas-zookeeper-manager && docker rm aeneas-zookeeper-manager
```

The first three containers must be started a bit after each other so they have time to start up
and register themselves. Otherwise, they will not connect and configuring the quorum will fail
when the manager is started.

Containers can be specified with either shorthand identifiers like `2` or full container names like `aeneas-zookeeper-2`
(or any other container name). When creating a ZooKeeper node container, ports will be inferred from the identifier.

If the manager is started in the foreground (without the `-d` flag), its logs will be streamed to the console and the container
will be removed at exit. This is perfectly fine since, unlike the nodes, it has no persistent state.

### Gradle

Though Aeneas is intended for use with multiple languages and platforms,
[Gradle](http://gradle.org/) is the overall build system. It calls into other
tools as necessary and handles multi-project configuration.
