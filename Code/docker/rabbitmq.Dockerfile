FROM rabbitmq:3.7-management
RUN rabbitmq-plugins enable --offline rabbitmq_management rabbitmq_stomp rabbitmq_web_stomp rabbitmq_mqtt
COPY config/rabbitmq.config /etc/rabbitmq/rabbitmq.config
