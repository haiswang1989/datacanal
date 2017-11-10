#!/bin/sh

host=${0}
port=${1}
username=${2}
password=${3}
dbname=${4}
sensitiveTables=${5}
zkPath=${6}

curr_dir=${pwd}
LIB="${curr_dir}/lib/"
JAVA_OPTS="-Xms1024 -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=1 -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/mtdperf.hprof -server -Dfile.encoding=UTF-8"
main_class=com.canal.instance.CDCEngine

nohup java ${JAVA_OPTS} -server -classpath "${LIB}/*" ${START_CLASS}  -h ${host} -p ${port} -u ${username} -pw ${password} -n ${dbname} -st ${sensitiveTables} -zp ${zkPath} &
