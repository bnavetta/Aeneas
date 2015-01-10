FROM debian:sid

RUN apt-get update \
	&& apt-get install -y curl  \
	&& rm -rf /var/lib/apt/lists/*

ENV MESOS_VERSION 0.21.0
ENV MESOS_DOWNLOAD_URL http://apache.claz.org/mesos/0.21.0/mesos-0.21.0.tar.gz
ENV MESOS_DOWNLOAD_SHA1 52cc62bd83903f42c289ecb647368881f918fa02

RUN buildDeps='build-essential maven'; \
    deps='openjdk-8-jdk python-dev python-boto libcurl4-nss-dev libsasl2-dev libapr1 libsvn-dev libapr1-dev zlib1g-dev'; \
    set -x \
    && apt-get update && apt-get install -y --no-install-recommends $buildDeps $deps \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /usr/src/mesos \
    && curl -sSL "$MESOS_DOWNLOAD_URL" -o mesos.tar.gz \
    && echo "$MESOS_DOWNLOAD_SHA1 *mesos.tar.gz" | sha1sum -c - \
    && tar -xzf mesos.tar.gz -C /usr/src/mesos --strip-components=1 \
    && rm mesos.tar.gz \
    && cd /usr/src/mesos \
    && ./configure \
    && make \
    && make install \
    && cd .. \
    && rm -r /usr/src/mesos \
    && apt-get purge -y --auto-remove $buildDeps
