## ZooKeeper Shared Configuration

ZooKeeper setup is stored under `/aeneas/zookeeper` in etcd.

### `state`

The `state` key keeps track of whether or not the cluster is
initialized.  It is used at node startup to determine whether any
other nodes have started, as in the
[etcd discovery protocol](https://github.com/coreos/etcd/blob/master/Documentation/discovery-protocol.md). If
it exists and has a value of `started`, then the cluster is running.

### `nodes`

The `nodes` directory contains a listing of current nodes. Keys are
server IDs and values are server specifications
(`<address1>:<port1>:<port2>[:role];[<client port address>:]<client
port>`). When registering, nodes must ensure that the same ID is used
each time and that it does not conflict with that of other nodes.

etcd has support for [in-order keys](https://coreos.com/docs/distributed-configuration/etcd-api/), but that doesn't allow for stable IDs.

[Exhibitor](https://github.com/Netflix/exhibitor) is a ZooKeeper
co-process with some of the features here. It has automatic instance
management, but based on rolling restarts. It can probably run with
Aeneas' ZooKeeper reconfiguration, and might be a good thing to build
into the ZooKeeper container, maybe with support for using etcd as a
configuration source.

### `ids`

To facilitate the ID requirements outlined in the [`nodes`](#nodes)
section, servers can track their IDs in etcd. The `ids` directory maps
IP addresses to server ID numbers, so servers can look up their
previously-used ID when registering.
