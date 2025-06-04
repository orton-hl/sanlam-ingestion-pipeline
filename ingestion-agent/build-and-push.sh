#!/bin/sh
docker build -t ghcr.io/orton-hl/sanlam-ingestion-agent:v1.0.0 .
docker push ghcr.io/orton-hl/sanlam-ingestion-agent:v1.0.0
