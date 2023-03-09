package mqt2rmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RmqHelper {
    private String host = "localhost";
    private int port = 5672;
    private String exchange = "rita";
    private ConnectionFactory factory;
    private Connection connection = null;
    private Channel channel = null;
    private String queueName = null;
    private boolean ready = false;
    private MqttHelper mqttHelper = null;

    public RmqHelper(@NotNull String host, int port, @NotNull String exchange) {
        this.host = host;
        this.port = port;
        this.exchange = exchange;

        this.factory = new ConnectionFactory();
        factory.setHost(this.host);
        System.out.println("RMQ Config: " + host + " " + port + " " + exchange);
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(this.exchange, "topic");
            this.queueName = channel.queueDeclare().getQueue();
            this.setupSubscriptions();
            ready = true;
        } catch (TimeoutException | IOException e) {
            System.err.println("Error creating new rmq connection: " + e.getMessage());
//            e.printStackTrace();
            ready = false;
        }
    }

    public void setupSubscriptions() {
        try {
            this.channel.queueBind(this.queueName, this.exchange, "interventions");
        } catch (IOException e) {
            System.out.println("Error when queueBind setting up subscription for topic 'interventions'.\n" + e.getMessage());
            e.printStackTrace();
        }

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            this.mqttHelper.sendMessage(message);
        };

        try {
            this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMqttHelper(MqttHelper val) {
        this.mqttHelper = val;
    }

    public void publishMessage(@NotNull String routingKey, @NotNull String message) throws IOException {
        // TODO_: Depending on the threading approach for publishers and subscribers,
        //   you might want to create a new channel here.
        // This approach seems to be working! 3/19/2020
        this.channel.basicPublish(this.exchange, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
//        System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
    }

    public static RmqHelper makeRMQ(@NotNull String host, int port, @NotNull String exchange) {
        RmqHelper rmq = new RmqHelper(host, port, exchange);
        if (rmq.ready) {
            return rmq;
        }
        return null;
    }
}
