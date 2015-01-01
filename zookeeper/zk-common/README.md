## ZooKeeper Clustering

The `zk-common` module defines the shared implementation of Aeneas' ZooKeeper cluster discovery system.

### Design

ZooKeeper server ids cannot be considered synonymous with IP addresses or hostnames, since neither is permanent.
Multiple servers can run on one host, so IP addresses and hostnames are not necessarily unique either. Instead, ids are
associated with ZooKeeper data directories. Therefore, a server uses the existing id if available (i.e. if the `myid`
file exists) and obtains a new id from the central registry if not.

#### Servers

When a server starts up, it publishes its information by adding an entry to the `/aeneas/zookeeper/servers` directory.
The entry key is the server id, and the value is a JSON object with the following information:

* An address (`address`)
* A role (`role`)
* A peer port (`peerPort`)
* An election port (`electionPort`)
* A client port (`clientPort`)

When the server terminates, it removes this entry.

#### Id Allocation

Etcd supports
[atomic in-order keys](https://coreos.com/docs/distributed-configuration/etcd-api/#atomically-creating-in-order-keys).
However, these keys cannot be generated from the `servers` directory, since an entry there indicates that the server is
ready to join the quorum. A server requesting an id cannot be ready to join because it does not have an id. Therefore,
a scratch directory (`/aeneas/zookeeper/idgen`) is used to obtain new ids. Servers can create in-order keys from this
directory with whatever value they like, since the returned key is atomically created and therefore unique.