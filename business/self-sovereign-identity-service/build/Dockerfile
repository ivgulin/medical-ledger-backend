FROM ubuntu:18.04
ARG uid=1000
ARG indy_stream=stable

ENV LC_ALL="C.UTF-8"
ENV LANG="C.UTF-8"
ENV SHELL="/bin/bash"

RUN \
  apt-get update && apt-get install -y software-properties-common && \
  add-apt-repository -y ppa:openjdk-r/ppa && \
  apt-get update && \
  apt-get install -y openjdk-8-jdk && \
  rm -rf /var/lib/apt/lists/* && \
  mkdir data && export JAVA_HOME='/usr/lib/jvm/java-8-openjdk-amd64' && \
  echo ${TEST_DIR_PATH} && cd ${TEST_DIR_PATH} && \
  touch 1 && cd ../ && ls ${TEST_DIR_PATH} && rm -rf ${TEST_DIR_PATH} && \
  apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 68DB5E88 && \
  add-apt-repository "deb https://repo.sovrin.org/sdk/deb xenial stable" && \
  apt-get update -y && apt-get install -y libindy && apt-get autoremove

COPY business/self-sovereign-identity-service/target/self-sovereign-identity-service-1.0.jar target/
CMD ["java", "-jar", "target/self-sovereign-identity-service-1.0.jar"]