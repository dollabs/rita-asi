package mqt2rmq;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

// Intervention topics
//agent/intervention/<unique_agent_name>/chat
//        agent/intervention/<unique_agent_name>/block
//        agent/intervention/<unique_agent_name>/map
class MqttHelper implements MqttCallback {

    private BlockingQueue<String> outq;
    private String host;
    private int port;
    private String server_uri;
    //    private MqttConnectOptions connOpts;
    private MqttClient client;
    private String uniqId = MqttClient.generateClientId();
    private String[] topics = {"#"};
    static int[] qos = {2}; // Note. This array must have the same elements as topics/
//    {"observations/#", "chat/#", "trial/#", "physiological/#", "measures/#",
//            "control/#", "agent/#", "experiment/#", "status/#", "ground_truth/#"};

    public String agentName = "Agent_RITA";
    HashMap<String, String> interventionLookup = new HashMap<String, String>() {{
        put("Minecraft_Chat", "agent/intervention/" + agentName + "/chat");
        put("Minecraft_Block", "agent/intervention/" + agentName + "/block");
        put("Client_Map", "agent/intervention/" + agentName + "/map");
    }};
    private final Gson gson = new Gson();

    MqttHelper(@NotNull BlockingQueue<String> outq, @NotNull String host,
               int port) throws MqttException {
        this.outq = outq;
        this.host = host;
        this.port = port;
        this.server_uri = "tcp://" + host + ":" + port;
        System.out.println("MQTT Server URI:" + this.server_uri);

        this.client = new MqttClient(this.server_uri, this.uniqId, new MemoryPersistence());
        this.client.connect();
        this.client.setCallback(this);
        this.client.subscribe(this.topics, qos);
        System.out.println("MQTT Subscription topics:");
        for (String topic : this.topics) {
            System.out.println(topic);
        }
        System.out.println("MQTT Waiting for messages with client id: " + this.uniqId);
//        System.out.println("Clean session should be true " + this.cl);
//        this.connOpts = new MqttConnectOptions();
//        connOpts.setCleanSession(true);
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
        try {
            this.client.reconnect();
            System.out.println("Reconnected because: " + cause);
        } catch (MqttException e) {
            System.out.print("mqtt connection lost and reconnect failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

        String msg = new String(message.getPayload());
//        System.out.println(String.format("[%s] %s", topic, msg));
        this.outq.offer(msg);
        int oqsize = this.outq.size();
        if (oqsize > 0 && oqsize % 250 == 0) {
            System.out.println("MqttHelper outq size is " + this.outq.size());
        }
    }

    public void sendMessage(String msg) {
        // parse json message as received from RMQ
        TreeMap<String, Object> data = gson.fromJson(msg, TreeMap.class);
//        "header": {
//            "timestamp": "2021-01-27T19:58:54.921148Z",
//                    "message_type": "agent",
//                    "version": "0.1"
//        }
        // add header
        TreeMap<String, Object> header = new TreeMap<>();
        String utctime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        header.put("timestamp", utctime);
        header.put("message_type", "agent");
        header.put("version", "0.1");
        data.put("header", header);
        sendMessage(data);
    }

    private void sendMessage(TreeMap<String, Object> message) {
        // Send to appropriate topic on MQTT
        String renderer = null;
        if (message.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) message.get("data");
            renderer = (String) data.get("renderer");
            if (renderer == null) {
                System.out.println("Warn: Intervention message does not has 'renderer' value");
            }
        }
        if (renderer != null) {
            String intervention_topic = interventionLookup.get(renderer);
            if (intervention_topic != null) {
                sendMessage(intervention_topic, gson.toJson(message));
            } else {
                System.out.println("Unknown renderer: " + renderer + " topic not found\n" + interventionLookup);
            }
        }
    }

    private void sendMessage(String topic, String message) {
        int qos = 2; // Exact delivery to each subscribed client.
        boolean retained = false; // message should not be retained by the server
        try {
            client.publish(topic, message.getBytes(), qos, retained);
        } catch (MqttException e) {
            System.out.println("Error sending message to MQTT topic: " + topic);
            System.out.println("The Message:\n" + message);
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
