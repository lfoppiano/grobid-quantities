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

FROM openjdk:8u275-jdk as builder

USER root

RUN apt-key del 7fa2af80 && \
    wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb && \
    dpkg -i cuda-keyring_1.0-1_all.deb && \
    apt-get update && \
    apt-get -y --no-install-recommends install apt-utils libxml2 git

RUN git clone https://github.com/kermitt2/grobid.git /opt/grobid-source && cd /opt/grobid-source && git checkout 0.7.1
WORKDIR /opt/grobid-source
COPY gradle.properties .

RUN git clone https://github.com/kermitt2/grobid-quantities.git ./grobid-quantities && cd grobid-quantities
WORKDIR /opt/grobid-source/grobid-quantities
COPY gradle.properties .

# Adjust config
RUN sed -i '/#Docker-ignore-log-start/,/#Docker-ignore-log-end/d'  ./resources/config/config.yml

# Preparing models
RUN rm -rf /opt/grobid-source/grobid-home/models/*

WORKDIR /opt/grobid-source/grobid-quantities
RUN ./gradlew clean assemble --no-daemon  --info --stacktrace
#RUN ./gradlew installScibert --no-daemon --info --stacktrace && rm -f /opt/grobid-source/grobid-home/models/*.zip
RUN ./gradlew copyModels --no-daemon --info --stacktrace && rm -f /opt/grobid-source/grobid-home/models/*.tar.gz


WORKDIR /opt

# -------------------
# build runtime image
# -------------------

FROM grobid/grobid:0.7.1 as runtime

# setting locale is likely useless but to be sure
ENV LANG C.UTF-8

# install JRE 8, python and other dependencies
RUN apt-key del 7fa2af80 && \
    curl https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2004/x86_64/cuda-keyring_1.0-1_all.deb -O cuda-keyring_1.0-1_all.deb && \
    dpkg -i cuda-keyring_1.0-1_all.deb && \
    rm /etc/apt/sources.list.d/cuda.list && \
    rm /etc/apt/sources.list.d/nvidia-ml.list

RUN apt-get update && \
    apt-get -y --no-install-recommends install git wget
#    apt-get -y remove python3.6 && \
#    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends tzdata && \
#    apt-get -y --no-install-recommends install git python3.7 python3.7-venv python3.7-dev python3.7-distutil

WORKDIR /opt/grobid

RUN mkdir -p /opt/grobid/grobid-quantities/resources/clearnlp/models /opt/grobid/grobid-quantities/resources/clearnlp/config
COPY --from=builder /opt/grobid-source/grobid-home/models ./grobid-home/models
COPY --from=builder /opt/grobid-source/grobid-quantities/build/libs/* ./grobid-quantities/
COPY --from=builder /opt/grobid-source/grobid-quantities/resources/config/config.yml ./grobid-quantities/
COPY --from=builder /opt/grobid-source/grobid-quantities/resources/clearnlp/config/* ./grobid-quantities/resources/clearnlp/config
COPY --from=builder /opt/grobid-source/grobid-quantities/resources/clearnlp/models/* ./grobid-quantities/resources/clearnlp/models

VOLUME ["/opt/grobid/grobid-home/tmp"]

# Install requirements
WORKDIR /opt/grobid/grobid-quantities

RUN sed -i 's/pythonVirtualEnv:.*/pythonVirtualEnv: /g' config.yml
RUN sed -i 's/grobidHome:.*/grobidHome: ..\/grobid-home/g' config.yml

# JProfiler
#RUN wget https://download-gcdn.ej-technologies.com/jprofiler/jprofiler_linux_12_0_2.tar.gz -P /tmp/ && \
#  tar -xzf /tmp/jprofiler_linux_12_0_2.tar.gz -C /usr/local &&\
#  rm /tmp/jprofiler_linux_12_0_2.tar.gz

EXPOSE 8060 8061
#EXPOSE 8080 8081

#CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005", "-jar", "grobid-superconductors/grobid-superconductors-0.2.1-SNAPSHOT-onejar.jar", "server", "grobid-superconductors/config.yml"]
#CMD ["java", "-agentpath:/usr/local/jprofiler12.0.2/bin/linux-x64/libjprofilerti.so=port=8849", "-jar", "grobid-superconductors/grobid-superconductors-0.2.1-SNAPSHOT-onejar.jar", "server", "grobid-superconductors/config.yml"]
CMD ["java", "-jar", "grobid-quantities-0.7.1-SNAPSHOT-onejar.jar", "server", "config.yml"]

ARG GROBID_VERSION


LABEL \
    authors="Luca Foppiano, Patrice Lopez" \
    org.label-schema.name="grobid-quantities" \
    org.label-schema.description="Docker image for grobid-quantities service" \
    org.label-schema.url="https://github.com/kermitt2/grobid-quantities" \
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

