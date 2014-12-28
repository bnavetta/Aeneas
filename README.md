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

### Gradle

Though Aeneas is intended for use with multiple languages and platforms,
[Gradle](http://gradle.org/) is the overall build system. It calls into other
tools as necessary and handles multi-project configuration.
