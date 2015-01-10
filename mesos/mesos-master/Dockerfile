FROM mesos

ADD build/libs/mesos-master-capsule.jar /opt/mesos-master.jar

EXPOSE 5050
VOLUME /var/lib/mesos/master

CMD ["java", "-jar", "/opt/mesos-master.jar"]