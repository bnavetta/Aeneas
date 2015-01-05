## Mesos Cluster

Since Aeneas supports ZooKeeper cluster discovery, Mesos can be run in
[high availability](http://mesos.apache.org/documentation/latest/high-availability/)
mode. In addition to the increased reliability, this has the advantage
of reducing the work on Aeneas' side. Masters and slaves will discover
each other and coordinate through ZooKeeper, so Aeneas simply needs to
point them to the ZooKeeper cluster.

`/aeneas/mesos` is used as the root path in ZooKeeper.

### Docker Containerizer

In many cases, Mesos'
[Docker containerizer](http://mesos.apache.org/documentation/latest/docker-containerizer/)
will be useful. This requires that the Docker command line be
installed in all slave containers. It also requires that the host's
Docker socket be mounted in the container along with some other
locations, as demonstrated
[here](https://github.com/redjack/docker-mesos/tree/master/mesos-slave/0.21.0). Another
option is to run a Docker daemon
[within the container](http://blog.docker.com/2013/09/docker-can-now-run-within-docker/). Ideally,
both situations will be supported. The process will look something like this:

1. Check for an existing Docker socket (`/var/run/docker.sock`) from the host
2. If there isn't one, try to start a Docker daemon within the container
3. If that fails, disable Mesos' Docker containerizer
