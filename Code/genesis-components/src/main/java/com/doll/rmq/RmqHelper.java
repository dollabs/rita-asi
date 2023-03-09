package doll.rmq;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class RmqHelper {
    private String host = "localhost";
    private int port = 5672;
    private String exchange = "rita";
    private ConnectionFactory factory = null;
    private Connection connection = null;
    private Channel channel = null;
    private String queueName = null;
    private boolean ready = false;

    public RmqHelper(String host, int port, String exchange) {
        this.host = host;
        this.port = port;
        this.exchange = exchange;

        this.factory = new ConnectionFactory();
        factory.setHost(this.host);

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(this.exchange, "topic");
            this.queueName = channel.queueDeclare().getQueue();
            this.setupSubscriptions();
            ready = true;
        } catch (TimeoutException e) {
            System.err.println("Error creating new rmq connection: " + e.getMessage());
//            e.printStackTrace();
            ready = false;
        } catch (IOException e) {
            System.err.println("Error creating new rmq connection: " + e.getMessage());
//            e.printStackTrace();
            ready = false;
        }
    }

    public void setupSubscriptions() throws IOException {
        this.channel.queueBind(this.queueName, this.exchange, "startup-rita");
        this.channel.queueBind(this.queueName, this.exchange, "testbed-message");
    }

    public void publishMessage(String routingKey, String message) throws IOException {
        // TODO: Depending on the threading approach for publishers and subscribers,
        //   you might want to create a new channel here.
        this.channel.basicPublish(this.exchange, routingKey, null, message.getBytes("UTF-8"));
//        System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
    }

    // TODO: Add JSON handling (constructing and deconstructing JSON objects), using the
    //   the standard Java JSON libraries
    // TODO: It might be useful to use the RMQ message definitions in
    //   resources/public/rabbitmq-messages.json for message validation and/or construction
    // TODO: Add support for multi-RMQ-message dispatch.
    //   One big case statement or multiple callback handlers?
    //   This file probably shouldn't contain the Genesis or RITA-specific logic for the messages.
    
    JSONParser parser = new JSONParser();
    
    public void waitForMessages() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            
            if (delivery.getEnvelope().getRoutingKey().toString().equals("testbed-message")) {
//                System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
				try {
					JSONObject obj = (JSONObject) parser.parse(message);
					obj = (JSONObject) obj.get("testbed-message");
					obj = (JSONObject) obj.get("data");
					HashMap<String, Object> data = new Gson().fromJson(obj.toString(), HashMap.class);
					for (String key: data.keySet()) {
						System.out.println(key + ": " + obj.get(key));
					}
					System.out.println();
	                
				} catch (ParseException e) {
					e.printStackTrace();
				}
            }
            // TODO: This is a simple example.  The mission-id should instead have one of the
            //   mission-ids pulled out of the received messages.
            String outputMessage = "{\"mission-id\":\"foo123\", \"timestamp\":1579874147854,\"routing-key\":\"predicted-next-steps\",\"app-id\":\"Genesis\", \"predicted-next-steps\":[1234, 5678, 9012]}";
            this.publishMessage("predicted-next-steps", outputMessage);
        };
        try {
            this.channel.basicConsume(this.queueName, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RmqHelper makeRMQ(String host, int port, String exchange) {
        RmqHelper rmq = new RmqHelper(host, port, exchange);
        if (rmq.ready == true) {
            return rmq;
        }
        return null;
    }
}
