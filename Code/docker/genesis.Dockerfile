FROM javabase

# Build Genesis
RUN mkdir $APPDIR/genesis-components
COPY genesis-components $APPDIR/genesis-components

WORKDIR $APPDIR/genesis-components

RUN ./gradlew clean && ./gradlew build

WORKDIR /

RUN mkdir $APPDIR/bin
COPY bin/ensurerabbitmq.sh $APPDIR/bin/ensurerabbitmq.sh

ENV LOGDIR $APPDIR/logs
RUN mkdir $LOGDIR

# Run Genesis
CMD echo "Running Genesis..."
WORKDIR $APPDIR/genesis-components
CMD $APPDIR/bin/ensurerabbitmq.sh java -jar build/libs/genesis-components-all.jar --exchange rita --host $RABBITMQ_HOST
