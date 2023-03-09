########## Working with this container

#This file is part of docker-compose setup. Working with docker-compose is priority than using it as an individual file
#For testing, we can build an image as
# In Code/ run following to build the image
# docker build -t tom-minecraft-test -f docker/tom.Dockerfile .

# On Mac,
# xhost +

## Specify -e DISPLAY=IP only when you need to run GUI from the docker. use the IP address of your Mac instead of 192.168.11.185
## specify -e RABBITMQ_HOST=IP where IP is the address of the machine running Rabbit broker.
# docker run --rm -it --network=host -e DISPLAY=192.168.11.185:0 -e RABBITMQ_HOST=192.168.11.185 tom-minecraft-test

#Note: --network=host starts the container in host mode which means the container will use RMQ instance running on the host.

########## Code Begin
FROM python:3.8

## Installs
RUN apt-get update -y && apt-get upgrade -y
RUN apt-get install python3-pip python3-dev ghostscript netcat -y
RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip3 install 'pika==0.13.1' pandas numpy matplotlib imageio jupyter scipy Pillow tqdm networkx moviepy future torch pdf2image

## Code and Config
ENV APPDIR /Applications

RUN mkdir -p $APPDIR/tom-minecraft
RUN mkdir -p $APPDIR/logs
COPY genesis-components/tom-minecraft $APPDIR/tom-minecraft

WORKDIR $APPDIR/tom-minecraft/gridworld

COPY bin/ensurerabbitmq.sh $APPDIR/ensurerabbitmq.sh

CMD $APPDIR/ensurerabbitmq.sh python3 -u ./rita_tom.py --host $RABBITMQ_HOST -m REPLAY_IN_RITA -i 0 > $APPDIR/logs/visualize.log

#CMD python visualize.py -m REPLAY_WITH_TOM -f ../../../test/data/ASIST_data_study_id_000001_condition_id_000005_trial_id_000015_messages.log -host $RABBITMQ_HOST -p 5672
#CMD bash

#parser = argparse.ArgumentParser(description='Parse arguments for the code.')
#parser.add_argument('-m', '--mode', type=str, default='', help='Mode of code you want to test')
#parser.add_argument('-f', '--file', type=str, default='', help='Relative path of log file you want to test')
#parser.add_argument('-d', '--directory', type=str, default='', help='Relative path of directory of log files that you want to test')
#parser.add_argument('-t', '--host', type=str, default='localhost', help='RabbitMQ host, if not localhost')
#parser.add_argument('-p', '--port', type=str, default='5672', help='RabbitMQ port, if not 5672')