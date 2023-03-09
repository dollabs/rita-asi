FROM pamelabase

ENV PATH /root/src/github/dollabs/planviz/bin:$PATH

WORKDIR /

##################### BIN SCRIPTS (enuremongodb) ################
RUN mkdir $APPDIR/bin
COPY code/bin/ensurerabbitmq.sh $APPDIR/bin/ensurerabbitmq.sh

ENV LOGDIR $APPDIR/logs
RUN mkdir $LOGDIR

EXPOSE ${PLANVIZ_PORT:-8080}

CMD $APPDIR/bin/ensurerabbitmq.sh planviz -v --exchange dcrypps --rmq-host $RABBITMQ_HOST --port ${PLANVIZ_PORT:-8080}
