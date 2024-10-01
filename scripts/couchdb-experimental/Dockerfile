# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

FROM eclipse-temurin:17-jdk-noble AS couchdb_build

ENV COUCHDB_VERSION=3.4.1

RUN rm -f /etc/apt/apt.conf.d/docker-clean
RUN --mount=type=cache,mode=0755,target=/var/cache/apt \
    apt-get -qq update

# be sure GPG and apt-transport-https are available and functional
RUN rm -f /etc/apt/apt.conf.d/docker-clean
RUN --mount=type=cache,mode=0755,target=/var/cache/apt \
    set -ex \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        ca-certificates \
        build-essential \
        pkg-config \
        libicu-dev \
        libssh-dev \
        unixodbc-dev \
        ncurses-dev \
        curl \
    && rm -rf /var/lib/apt/lists/*

# Prepare build environent
RUN mkdir -p $HOME/build/couchdb \
    && mkdir -p $HOME/build/erlang

# Download and build Erlang 27
RUN --mount=type=cache,mode=0755,target=/var/cache/apt \
    set -ex \
    && curl -L https://api.github.com/repos/erlang/otp/tarball/refs/tags/OTP-27.1 | tar -zx -C $HOME/build/erlang --strip-components=1 \
    && cd $HOME/build/erlang \
    && ./configure \
    && make -j$(nproc) \
    && make install

# Download CouchDB and build
RUN curl -L https://dlcdn.apache.org/couchdb/source/${COUCHDB_VERSION}/apache-couchdb-${COUCHDB_VERSION}.tar.gz | tar -xz -C $HOME/build/couchdb --strip-components=1 \
    && cd $HOME/build/couchdb \
    && ./configure \
        --with-nouveau \
        --js-engine=quickjs \
        --disable-spidermonkey \
    && make -j$(nproc) release

COPY default_couchdb-nouveau $HOME/build/couchdb/rel/couchdb/etc/default/couchdb-nouveau
COPY systemd-couchdb_service $HOME/build/couchdb/rel/couchdb/lib/systemd/system/couchdb-nouveau.service

RUN cd $HOME/build/couchdb/rel/couchdb \
    && mkdir -p /var/tmp \
    && tar -cJvf /var/tmp/couchdb-3.4.1-nouveau-noble.tar.xz *


FROM eclipse-temurin:17-jdk-noble AS runtime

LABEL maintainer="Helio Chissini de Castro heliocastro@gmail.com"

# be sure GPG and apt-transport-https are available and functional
RUN --mount=type=cache,mode=0755,target=/var/cache/apt \
    set -ex \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        apt-transport-https \
        ca-certificates \
        dirmngr \
        gnupg \
        xz-utils \
    && rm -rf /var/lib/apt/lists/*

# Copy CouchDB build from previous stage
COPY --from=couchdb_build /var/tmp/couchdb-3.4.1-nouveau-noble.tar.xz /tmp
RUN mkdir -p /opt/couchdb \
    && tar xvf /tmp/couchdb-3.4.1-nouveau-noble.tar.xz -C /opt/couchdb \
    && rm /tmp/couchdb-3.4.1-nouveau-noble.tar.xz

# Add CouchDB user account to make sure the IDs are assigned consistently
RUN groupadd -g 5984 -r nouveau && useradd -u 5984 -d /opt/nouveau -g nouveau nouveau

# grab tini for signal handling and zombie reaping
# see https://github.com/apache/couchdb-docker/pull/28#discussion_r141112407
RUN --mount=type=cache,mode=0755,target=/var/cache/apt \
    set -ex \
    && apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    tini; \
    rm -rf /var/lib/apt/lists/*; \
    tini --version

# https://github.com/apache/couchdb-pkg/blob/master/debian/README.Debian
RUN set -eux; \
    apt-get update; \
    \
    echo "couchdb-nouveau couchdb-nouveau/enable select false" | debconf-set-selections;

RUN chown -R nouveau:nouveau /opt/nouveau
COPY --chown=nouveau:nouveau nouveau.yaml /opt/nouveau/etc/nouveau.yaml

VOLUME /opt/nouveau/data

# 5987: Nouveau App
# 5988: Nouveau Admin
EXPOSE 5987 5988

# TODO: re-add tini
CMD ["/usr/bin/java", "-server", "-Djava.awt.headless=true", "-Xmx2g", "-jar", "/opt/nouveau/lib/nouveau-1.0-SNAPSHOT.jar", "server", "/opt/nouveau/etc/nouveau.yaml"]
