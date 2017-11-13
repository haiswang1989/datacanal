#!/bin/sh

host=${1}
port=${2}
username=${3}
password=${4}
dbname=${5}
sensitiveTables=${6}
zkPath=${7}

identity=${host}_${dbname}

for name in /home/instance/lib/*.jar  
do  
   CLASSPATH="$CLASSPATH":"$name"  
done  

JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=128m -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=1 -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/mtdperf.hprof -server -Dfile.encoding=UTF-8"
main_class=com.canal.instance.CDCEngine

nohup java ${JAVA_OPTS} -server -classpath ${CLASSPATH} ${main_class}  -h ${host} -p ${port} -u ${username} -pw ${password} -n ${dbname} -st ${sensitiveTables} -zp ${zkPath} 1>>${identity}.log 2>&1 &
echo $! > ${identity}.pid
