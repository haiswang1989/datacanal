#!/bin/sh

INSTANCE_LOG_PATH=/home/datacanal/datacanal-instance/bin

taskNodePath=${1}

identity=${taskNodePath////_}

JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=1 -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/mtdperf.hprof -server -Dfile.encoding=UTF-8"

main_class=com.canal.instance.code.CDCEngine

nohup java ${JAVA_OPTS} -server -classpath ${CLASSPATH} ${main_class} ${taskNodePath}  1>>${INSTANCE_LOG_PATH}/${identity}.log 2>&1 &
echo $! > ${INSTANCE_LOG_PATH}/${identity}.pid
