FROM ubuntu:xenial

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      mongodb \
      python3 \
      python3-dev \
      python3-pip \
      tmux \
      git \
      openjdk-8-jdk \
      wget

RUN pip3 install --upgrade pip && \
    pip3 install \
      bottle \
      pymongo

ENV LEIN_ROOT true
ENV LEIN_URL https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein

RUN wget -q -O /usr/bin/lein $LEIN_URL && \
    chmod +x /usr/bin/lein && \
    lein

# mongodb
EXPOSE 27017

# api entry
EXPOSE 8001

