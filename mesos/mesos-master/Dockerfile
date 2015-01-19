FROM mesos

ADD build/libs/mesos-master-capsule.jar /opt/mesos-master.jar

VOLUME /var/lib/mesos/master

CMD ["java", "-jar", "/opt/mesos-master.jar"]