FROM registry.gitlab.com/dollabsp/docker/pamelabase

RUN mkdir $APPDIR/JavaApps

# Copy the bin files and make sure they're executuable
RUN mkdir $APPDIR/bin
COPY bin $APPDIR/bin

WORKDIR $APPDIR/bin
RUN ls | grep .sh | xargs chmod +x

WORKDIR $APPDIR/JavaApps
COPY rmq-logger-tools/rmq-logger-0.2.0-SNAPSHOT.jar .
RUN mkdir $APPDIR/logs

CMD $APPDIR/bin/ensurerabbitmq.sh java -jar rmq-logger-0.2.0-SNAPSHOT.jar --exchange rita --host $RABBITMQ_HOST > $APPDIR/logs/rmq.log
