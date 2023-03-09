FROM registry.gitlab.com/dollabsp/docker/pamelabase

RUN apt-get update \
 && apt-get install -y python-pip supervisor


##################### DOLL-Components ###########################
RUN mkdir $APPDIR/doll-components
WORKDIR $APPDIR/doll-components
COPY doll-components/*boot* $APPDIR/doll-components/

RUN mkdir $APPDIR/doll-components/pamela
COPY doll-components/pamela $APPDIR/doll-components/pamela

RUN mkdir $APPDIR/doll-components/plan-schema
COPY doll-components/plan-schema $APPDIR/doll-components/plan-schema

RUN mkdir $APPDIR/doll-components/pamela-tools
COPY doll-components/pamela-tools $APPDIR/doll-components/pamela-tools

RUN mkdir $APPDIR/doll-components/BSP
COPY doll-components/BSP $APPDIR/doll-components/BSP

RUN mkdir $APPDIR/doll-components/drl007
COPY doll-components/drl007 $APPDIR/doll-components/drl007

RUN mkdir $APPDIR/doll-components/src
RUN mkdir $APPDIR/doll-components/resources
RUN boot jar

COPY doll-components $APPDIR/doll-components
## doll-components/resources/public is a symlink and newer version 3 of docker desktop is refusing to overwrite it.
RUN rm $APPDIR/doll-components/resources/public
COPY resources $APPDIR/doll-components/resources

RUN mkdir $APPDIR/doll-components/data
COPY data $APPDIR/doll-components/data

# This data is required at build time
RUN mkdir -p $APPDIR/evaluations/nov-2020
COPY evaluations/nov-2020 $APPDIR/evaluations/nov-2020
RUN mkdir -p $APPDIR/evaluations/study-2
COPY evaluations/study-2 $APPDIR/evaluations/study-2
RUN mkdir -p $APPDIR/evaluations/study-3
COPY evaluations/study-3 $APPDIR/evaluations/study-3

RUN boot build-jar

RUN mkdir $APPDIR/runtime-learned-models
# doll-components will write output relative to CWD
WORKDIR $APPDIR

##################### BIN SCRIPTS (enuremongodb) ################
RUN mkdir $APPDIR/bin
COPY bin/ensurerabbitmq.sh $APPDIR/bin/ensurerabbitmq.sh

ENV LOGDIR $APPDIR/logs
RUN mkdir $LOGDIR
COPY docker/supervisord-clojure.conf /etc/supervisor/conf.d/supervisord.conf

CMD /usr/bin/supervisord -c /etc/supervisor/conf.d/supervisord.conf
