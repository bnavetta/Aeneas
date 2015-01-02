FROM java:8

ENV ZOOKEEPER_VERSION 3.5.0-alpha
ENV ZOOKEEPER_DOWNLOAD_URL http://apache.tradebit.com/pub/zookeeper/zookeeper-3.5.0-alpha/zookeeper-3.5.0-alpha.tar.gz
ENV ZOOKEEPER_DOWNLOAD_SHA1 bde0295d02823e343f916600c8303f25982205ad

RUN set -x \
	&& mkdir -p /opt/zookeeper \
    && curl -sSL "$ZOOKEEPER_DOWNLOAD_URL" -o zookeeper.tar.gz \
    && echo "$ZOOKEEPER_DOWNLOAD_SHA1 *zookeeper.tar.gz" | sha1sum -c - \
    && tar -xzf zookeeper.tar.gz -C /opt/zookeeper --strip-components=1 \
    && rm zookeeper.tar.gz

ADD build/libs/zk-node-capsule.jar /opt/node.jar

RUN mkdir /var/lib/zookeeper
VOLUME /var/lib/zookeeper

EXPOSE 2888
EXPOSE 3888
EXPOSE 2181

CMD ["java", "-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", "-jar", "/opt/node.jar"]