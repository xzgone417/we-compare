#!/bin/sh
set -e

pid=$(jps | grep -F 'AnnualParty' | cut -d ' ' -f 1)
if [ -n "$pid" ]; then
    kill $pid
fi

mkdir -p ./log

java \
-server \
-Xms2g -Xmx2g \
-XX:OnOutOfMemoryError="kill -11 %p" \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+DisableExplicitGC \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:InitiatingHeapOccupancyPercent=50 \
-XX:G1ReservePercent=10 \
-XX:G1HeapWastePercent=10 \
-XX:G1HeapRegionSize=16M \
-XX:+UnlockExperimentalVMOptions \
-XX:G1NewSizePercent=5 \
-XX:G1MaxNewSizePercent=60 \
-XX:G1MixedGCLiveThresholdPercent=75 \
-XX:G1MixedGCCountTarget=32 \
-XX:NativeMemoryTracking=detail \
-Xlog:gc*=info:./log/gc.log:time,level,tags:filecount=5,filesize=100m \
-Dlogging.config=./logback.xml \
-jar ./AnnualParty-1.0.0.jar