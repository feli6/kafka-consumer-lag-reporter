FROM openjdk:alpine

RUN mkdir -p /app /app/logs

COPY web/target/kafka-consumer-lag-reporter.jar /app/app.jar

RUN chgrp -R 0 /app \
  && chmod -R g+rwX /app

CMD java \
  -Xms512m \
  -Xmx512m \
  -XX:MaxMetaspaceSize=256m \
  -XshowSettings:vm \
  -XX:+PrintGCDateStamps \
  -verbose:gc \
  -XX:+PrintGCDetails \
  -Xloggc:/app/logs/gc.log \
  -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=7 \
  -XX:GCLogFileSize=100M \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/ \
  -server \
  -Dcom.sun.management.jmxremote  \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Dcom.sun.management.jmxremote.port=1099 \
  -Dcom.sun.management.jmxremote.rmi.port=1099 \
  -Djava.rmi.server.hostname=127.0.0.1 \
  -Djava.security.egd=file:/dev/./urandom -jar \
  /app/app.jar