#!/bin/sh
BASE_DIR=`dirname $0`
PROJECT_DIR="${BASE_DIR}/.."
ETC_DIR="${PROJECT_DIR}/etc"

${JAVA_HOME}/bin/java -cp ${PROJECT_DIR}/@jarArchiveName@:${ETC_DIR} com.byteatebit.nbecho.perfclient.EchoPerfClient