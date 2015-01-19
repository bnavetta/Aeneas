FROM mesos

RUN apt-get update \
	&& apt-get install -y --no-install-recommends docker.io lxc iptables \
	&& rm -rf /var/lib/apt/lists/*

ADD build/libs/mesos-slave-capsule.jar /opt/mesos-slave.jar

# Required for Docker-in-Docker
VOLUME /var/lib/docker

CMD ["java", "-jar", "/opt/mesos-slave.jar"]