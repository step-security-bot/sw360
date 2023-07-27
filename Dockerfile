# Copyright Siemens AG, 2020. Part of the SW360 Portal Project.
# Copyright BMW CarIT GmbH, 2021.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

#--------------------------------------------------------------------------------------------------
# Thrift
FROM ubuntu:22.04 AS sw360thriftbuild

ARG BASEDIR="/build"
ARG THRIFT_VERSION

RUN --mount=type=cache,target=/var/cache/apt \
    apt-get -qq update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    bison \
    build-essential \
    cmake \
    curl \
    flex \
    libevent-dev \
    libtool \
    pkg-config \
    && rm -rf /var/lib/apt/lists/*

COPY ./scripts/install-thrift.sh build_thrift.sh

RUN --mount=type=tmpfs,target=/build \
    --mount=type=cache,target=/var/cache/deps \
    chmod a+x build_thrift.sh \
    && ./build_thrift.sh

FROM scratch AS sw360thrift
COPY --from=sw360thriftbuild /usr/local/bin/thrift /usr/local/bin/thrift

#--------------------------------------------------------------------------------------------------
# SW360 No Liferay
# We build sw360 and create real image after everything is ready
# So when decide to use as development, only this last stage
# is triggered by buildkit images

FROM maven:3-eclipse-temurin-11 as sw360build

ARG COUCHDB_HOST=localhost

# Thanks to Liferay, we need fix the java version
ENV _JAVA_OPTIONS='-Djdk.util.zip.disableZip64ExtraFieldValidation=true'

WORKDIR /build

SHELL ["/bin/bash", "-c"]

# Install mkdocs to generate documentation
RUN --mount=type=cache,target=/var/cache/apt \
    apt-get -qq update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    gettext \
    git \
    python3-pip \
    python3-wheel \
    zip \
    unzip \
    && rm -rf /var/lib/apt/lists/* \
    && pip install mkdocs-material

# Prepare maven from binary to avoid wrong java dependencies and proxy
COPY scripts/docker-config/mvn-proxy-settings.xml /etc
COPY scripts/docker-config/set_proxy.sh /usr/local/bin/setup_maven_proxy
RUN chmod a+x /usr/local/bin/setup_maven_proxy \
    && setup_maven_proxy

COPY --from=sw360thrift /usr/local/bin/thrift /usr/bin

RUN --mount=type=bind,target=/build/sw360,rw \
    --mount=type=cache,target=/root/.m2 \
    --mount=type=secret,id=sw360 \
    cd /build/sw360 \
    && set -a \
    && source /run/secrets/sw360 \
    && envsubst < scripts/docker-config/couchdb.properties.template | tee scripts/docker-config/etc_sw360/couchdb.properties \
    && envsubst < scripts/docker-config/couchdb-lucene.ini | tee third-party/couchdb-lucene/src/main/resources/couchdb-lucene.ini \
    && set +a \
    && cp scripts/docker-config/etc_sw360/couchdb.properties build-configuration/resources/ \
    && cp -a scripts/docker-config/etc_sw360 /etc/sw360 \
    && mkdir /etc/sw360/manager \
    && envsubst < scripts/docker-config/manager/tomcat-users.xml | tee /etc/sw360/manager/tomcat-users.xml \
    && mvn clean package \
    -P deploy \
    -Dtest=org.eclipse.sw360.rest.resourceserver.restdocs.* \
    -Dsurefire.failIfNoSpecifiedTests=false \
    -Dbase.deploy.dir=. \
    -Djars.deploy.dir=/sw360_deploy \
    -Dbackend.deploy.dir=/sw360_tomcat_webapps \
    -Drest.deploy.dir=/sw360_tomcat_webapps \
    -Dhelp-docs=true

# Generate slim war files
WORKDIR /sw360_tomcat_webapps/

COPY scripts/create-slim-war-files.sh /bin/slim.sh

RUN bash /bin/slim.sh

#--------------------------------------------------------------------------------------------------
# Runtime image without liferay
FROM tomcat:9-jdk11 AS sw360

ENV CATALINA_HOME=/usr/local/tomcat

# Modified etc
COPY --from=sw360build /etc/sw360 /etc/sw360
# Downloaded jar dependencies
COPY --from=sw360build /sw360_deploy/* /app/sw360/deploy
# Streamlined wars
COPY --from=sw360build /sw360_tomcat_webapps/slim-wars/*.war ${CATALINA_HOME}/webapps/
# org.eclipse.sw360 jar artifacts
COPY --from=sw360build /sw360_tomcat_webapps/*.jar ${CATALINA_HOME}/webapps/
# Shared streamlined jar libs
COPY --from=sw360build /sw360_tomcat_webapps/libs/*.jar ${CATALINA_HOME}/shared/

# Make catalina understand shared directory
RUN sed -i "s,shared.loader=,shared.loader=/usr/local/tomcat/shared/*.jar,g" /usr/local/tomcat/conf/catalina.properties
RUN sed -i -e 's/<Engine/<Engine startStopThreads="0" /g' -e 's/<Host/<Host startStopThreads="0" /g' /usr/local/tomcat/conf/server.xml

# Make manage gui available during test period
COPY scripts/docker-config/conf/tomcat-users.xml ${CATALINA_HOME}/conf/tomcat-users.xml
COPY scripts/docker-config/conf/Catalina/localhost/* ${CATALINA_HOME}/conf/Catalina/localhost/

# Copy liferay/sw360 config files
COPY --chown=$USERNAME:$USERNAME ./scripts/docker-config/portal-ext.properties /app/sw360/portal-ext.properties
COPY --chown=$USERNAME:$USERNAME ./scripts/docker-config/noliferay_entrypoint.sh /entrypoint.sh

# Tomcat manager for debugging portlets
COPY --chown=$USERNAME:$USERNAME --from=tomcat:9.0.56-jdk11 /usr/local/tomcat/webapps.dist/manager /app/sw360/tomcat/webapps/manager
RUN --mount=type=bind,target=/build/sw360,rw \
    if [  DEBUG ]; then \
    cp /etc/sw360/manager/tomcat-users.xml /app/sw360/tomcat/conf/tomcat-users.xml ; \
    cp /build/sw360/scripts/docker-config/manager/context.xml /app/sw360/tomcat/webapps/manager/META-INF/context.xml ; \
    else \
    mv /app/sw360/tomcat/webapps/manager /app/sw360/tomcat/webapps/manager.disabled ; \
    fi

STOPSIGNAL SIGINT

ENTRYPOINT [ "/entrypoint.sh" ]
