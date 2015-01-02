FROM java:8

ADD build/libs/zk-manager-capsule.jar /opt/manager.jar

CMD ["java", "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", "-jar", "/opt/manager.jar"]