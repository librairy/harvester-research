#!/bin/bash
export MAVEN_OPTS="-Xmx8g"
now=$(date +"%Y%m%d-%H%M%S")
mvn -Dtest="UpfGateProcessorTest#annotateDocuments" test
