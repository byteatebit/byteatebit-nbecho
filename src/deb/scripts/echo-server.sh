#!/bin/sh
BASE_DIR=`dirname $0`
PROJECT_DIR="${BASE_DIR}/.."
ETC_DIR="${PROJECT_DIR}/etc"

${JAVA_HOME}/bin/java -cp ${PROJECT_DIR}/@jarArchiveName@:${ETC_DIR} -XX:+UseParNewGC \
-XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+ParallelRefProcEnabled \
-XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=32768 \
com.byteatebit.nbecho.NbEchoServer $*