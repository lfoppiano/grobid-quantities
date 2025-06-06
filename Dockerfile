## Docker GROBID-quantities image using deep learning models and/or CRF models, and various python modules
## Borrowed from https://github.com/kermitt2/grobid/blob/master/Dockerfile.delft
## See https://grobid.readthedocs.io/en/latest/Grobid-docker/

## usage example with grobid: https://github.com/kermitt2/grobid/blob/master/Dockerfile.delft

## docker build -t lfoppiano/grobid-quantities:0.7.0 --build-arg GROBID_VERSION=0.7.0 --file Dockerfile .

## no GPU:
## docker run -t --rm --init -p 8060:8060 -p 8061:8061 -v config.yml:/opt/grobid/grobid-quantities:ro  lfoppiano/grobid-quantities:0.7.1

## allocate all available GPUs (only Linux with proper nvidia driver installed on host machine):
## docker run --rm --gpus all --init -p 8072:8072 -p 8073:8073 -v grobid.yaml:/opt/grobid/grobid-home/config/grobid.yaml:ro  lfoppiano/grobid-superconductors:0.3.0-SNAPSHOT

# -------------------
# build builder image
# -------------------

FROM openjdk:17-jdk-slim as builder

USER root

RUN apt-get update && \
    apt-get -y --no-install-recommends install apt-utils libxml2 git-lfs unzip

WORKDIR /opt/grobid

RUN mkdir -p grobid-quantities-source grobid-home/models
COPY src grobid-quantities-source/src
COPY settings.gradle grobid-quantities-source/
COPY resources/config/config-docker.yml grobid-quantities-source/resources/config/config.yml
COPY resources/models grobid-quantities-source/resources/models
COPY resources/clearnlp/models/* grobid-quantities-source/resources/clearnlp/models/
COPY build.gradle grobid-quantities-source/
COPY gradle.properties grobid-quantities-source/
COPY gradle grobid-quantities-source/gradle/
COPY gradlew grobid-quantities-source/
COPY .git grobid-quantities-source/.git
COPY localLibs grobid-quantities-source/localLibs

# Preparing models
WORKDIR /opt/grobid/grobid-quantities-source
RUN rm -rf /opt/grobid/grobid-home/models/*
RUN ./gradlew clean assemble -x shadowJar --no-daemon  --stacktrace --info
RUN git lfs install
RUN ./gradlew installModels --no-daemon --info --stacktrace \
    && rm -f /opt/grobid/grobid-home/models/*.zip \
    && rm -rf /opt/grobid/grobid-home/models/quantities_models

# Preparing distribution
WORKDIR /opt/grobid
RUN unzip -o /opt/grobid/grobid-quantities-source/build/distributions/grobid-quantities-*.zip -d grobid-quantities_distribution \
  && mv grobid-quantities_distribution/grobid-quantities-* grobid-quantities

# Cleanup 
RUN rm -rf grobid-quantities-source/.git \
    && rm -rf /opt/grobid/grobid-quantities-source/build/distributions/grobid-quantities-*.zip \
    && rm -rf grobid-quantities-source/build

WORKDIR /opt

# -------------------
# build runtime image
# -------------------

FROM lfoppiano/grobid:0.8.2-full as runtime

# setting locale is likely useless but to be sure
ENV LANG C.UTF-8

RUN apt-get update && \
    apt-get -y --no-install-recommends install git wget 

WORKDIR /opt/grobid

RUN mkdir -p /opt/grobid/grobid-quantities/resources/clearnlp/models /opt/grobid/grobid-quantities/resources/clearnlp/config
COPY --from=builder /opt/grobid/grobid-home/models ./grobid-home/models
COPY --from=builder /opt/grobid/grobid-quantities ./grobid-quantities/
COPY --from=builder /opt/grobid/grobid-quantities-source/resources/config/config.yml ./grobid-quantities/resources/config/
COPY --from=builder /opt/grobid/grobid-quantities-source/resources/clearnlp/models/* ./grobid-quantities/resources/clearnlp/models/

VOLUME ["/opt/grobid/grobid-home/tmp"]

RUN ln -s /opt/grobid/grobid-quantities/resources /opt/grobid/resources

# JProfiler
#RUN wget https://download-gcdn.ej-technologies.com/jprofiler/jprofiler_linux_12_0_2.tar.gz -P /tmp/ && \
#  tar -xzf /tmp/jprofiler_linux_12_0_2.tar.gz -C /usr/local &&\
#  rm /tmp/jprofiler_linux_12_0_2.tar.gz

WORKDIR /opt/grobid
ARG GROBID_VERSION
ENV GROBID_VERSION=${GROBID_VERSION:-latest}
ENV GROBID_QUANTITIES_OPTS "-Djava.library.path=/opt/grobid/grobid-home/lib/lin-64:/usr/local/lib/python3.8/dist-packages/jep --add-opens java.base/java.lang=ALL-UNNAMED"

# This code removes the fixed seeed in DeLFT to increase the uncertanty 
#RUN sed -i '/seed(7)/d' /usr/local/lib/python3.8/dist-packages/delft/utilities/Utilities.py
#RUN sed -i '/from numpy\.random import seed/d' /usr/local/lib/python3.8/dist-packages/delft/utilities/Utilities.py

EXPOSE 8060 8061 5005

#CMD ["java", "-agentpath:/usr/local/jprofiler12.0.2/bin/linux-x64/libjprofilerti.so=port=8849", "-jar", "grobid-superconductors/grobid-quantities-${GROBID_VERSION}-onejar.jar", "server", "grobid-superconductors/config.yml"]
#CMD ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:5005 -jar grobid-quantities/grobid-quantities-${GROBID_VERSION}-onejar.jar server grobid-quantities/config.yml"]
#CMD ["sh", "-c", "java -jar grobid-quantities/grobid-quantities-${GROBID_VERSION}-onejar.jar server grobid-quantities/config.yml"]
CMD ["./grobid-quantities/bin/grobid-quantities", "server", "grobid-quantities/resources/config/config.yml"]


LABEL \
    authors="Luca Foppiano, Patrice Lopez" \
    org.label-schema.name="grobid-quantities" \
    org.label-schema.description="Docker image for grobid-quantities service" \
    org.label-schema.url="https://github.com/lfoppiano/grobid-quantities" \
    org.label-schema.version=${GROBID_VERSION}


## Docker tricks:

# - remove all stopped containers
# > docker rm $(docker ps -a -q)

# - remove all unused images
# > docker rmi $(docker images --filter "dangling=true" -q --no-trunc)

# - remove all untagged images
# > docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

# - "Cannot connect to the Docker daemon. Is the docker daemon running on this host?"
# > docker-machine restart

