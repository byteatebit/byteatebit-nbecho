#
# Oracle Java 8 Dockerfile
#
# https://github.com/dockerfile/java
# https://github.com/dockerfile/java/tree/master/oracle-java8
#

# Pull base image.
FROM ubuntu

# Install add-apt-repository

RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
  apt-get install -yq software-properties-common

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV PATH "${PATH}:${JAVA_HOME}/bin"

COPY @packageName@_@packageVersion@-1_all.deb /tmp/stage/
RUN dpkg -i /tmp/stage/@packageName@_@packageVersion@-1_all.deb

# Define default command.
CMD ["/opt/byteatebit-nbecho/scripts/echo-server.sh"]
