# syntax=docker/dockerfile:1.4
#
# Copyright Siemens AG, 2020. Part of the SW360 Portal Project.
# Copyright BMW CarIT GmbH, 2021.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

#-----------------------------------------------------------------------------------
# Base image
# We need use JDK, JRE is not enough as Liferay do runtime changes and require javac
FROM maven:3-eclipse-temurin-17 AS base

ENV COUCHDB_USER="admin"
ENV COUCHDB_PASSWORD="password"

COPY ./build-configuration/test-resources/couchdb-test.properties /etc/sw360/couchdb-test.properties
COPY ./scripts/sw360BackendRestDockerConfig/etc_sw360/rest-test.properties /etc/sw360/rest-test.properties

COPY --from=eclipse-sw360/thrift /usr/local/bin/thrift /usr/local/bin/thrift

RUN sed -i 's/^couchdb.user\s*=/& '${COUCHDB_USER}'/' /etc/sw360/couchdb-test.properties \
    && sed -i 's/^couchdb.password\s*=/& '${COUCHDB_PASSWORD}'/' /etc/sw360/couchdb-test.properties

RUN --mount=type=cache,target=/var/cache/apt \
    apt-get update -qq \
    && DEBIAN_FRONTEND=noninteractive apt-get install -qq -y --no-install-recommends \
    gettext-base \
    git \
    python3-pip \
    python3-wheel \
    zip \
    unzip \
    && rm -rf /var/lib/apt/lists/* \
    && pip install mkdocs-material