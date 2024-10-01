#!/bin/bash

docker buildx build \
    --tag ghcr.io/eclipse-sw360/couchdb:3.4.1-nouveau-noble \
    --target couchdb_build \
    "$@" \
    .
