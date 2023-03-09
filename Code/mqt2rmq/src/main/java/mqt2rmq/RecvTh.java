package mqt2rmq;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RecvTh extends Thread {

    private RmqHelper rmq;
    private BlockingQueue<String> incoming;
    public boolean done = false;
    private long sofar = 0;
    private long last_clock_time = 0;
    private String last_clock_time_string = null;
    private long last_mission_clock_time = 0;
    private int last_clock_freq = 2; //Hz
    private int last_clock_delay = 500; // 1 / freq
    private final Gson gson = new Gson();

    private boolean mission_timer_clock_active = false;
    private long mission_timer_clock_max_value = 0;
    private String mission_timer_value = null;
    private ZonedDateTime mission_timer_clock_epoc_utc = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    public RecvTh(@NotNull RmqHelper rmq, @NotNull BlockingQueue<String> incoming) {
        this.rmq = rmq;
        this.incoming = incoming;
    }

    public void setClockFrequency(int fq) {
        if (fq != 0) {
            last_clock_freq = fq;
            last_clock_delay = (int) (1000 * 1 / fq);
            System.out.println("Clock message frequency set to: " + fq + " Min delay: " + last_clock_delay);
        }
    }

    public void run() {
        System.out.println("Starting thread to handle messages from blocking q and publish to rmq");
        while (!this.done) {
            try {
                String msg = this.incoming.poll(1, TimeUnit.SECONDS);
                if (msg != null) {
                    try {
                        this.publish(msg);
                    } catch (Exception e) {
                        System.out.println("Message: " + msg);
                        System.out.println("Exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {

                if (this.incoming.size() == 0) {
                    this.done = true;
                } else {
                    System.out.println("RecvTh interrupted: " + e.getMessage() + " but queue has " + this.incoming.size() + " elements");
                }
//                e.printStackTrace();
            }
        }
    }

    private TreeMap<String, Object> makeClockMsg(String as_str, String routing_key) {
        // as_str is time in ISO 8601 format: YYYY-MM-DDThh:mm:ss.ssssZ
        long tim = java.time.Instant.parse(as_str).toEpochMilli();
        if (routing_key.equals("clock") && tim < last_clock_time) {
            System.out.println("Clock has moved backwards:(in sec) " + (last_clock_time - tim) / 1000.0 +
                    " now: " + as_str + " before: " + last_clock_time_string);
        }
        if (routing_key.equals("clock")) {
            last_clock_time = tim;
            last_clock_time_string = as_str;
        }

        if (routing_key.equals("mission_clock") && tim < last_mission_clock_time) {
            System.out.println("Mission Clock has moved backwards: " + as_str + " (in sec) " + (last_mission_clock_time - tim) / 1000.0);
        }
        if (routing_key.equals("mission_clock")) {
            last_mission_clock_time = tim;
        }

        TreeMap<String, Object> rmq_msg = new TreeMap<String, Object>();
        rmq_msg.put("tb_clock", as_str);
        rmq_msg.put("timestamp", tim);
        rmq_msg.put("app-id", "TestbedBusInterface");
        return rmq_msg;
    }

    private TreeMap<String, Object> makeClockMsgFromHeader(TreeMap<String, Object> data) {
        if (data.containsKey("header")) {
            Map<String, Object> header_obj = (Map) data.get("header");
            if (header_obj.containsKey("timestamp")) {
                String testbed_time_str = (String) header_obj.get("timestamp");
                return this.makeClockMsg(testbed_time_str, "clock");
            }
        }
        return null;
    }

    private TreeMap<String, Object> makeClockMsgFromMissionTimer(TreeMap<String, Object> tb_msg) {
        // Extract mission timer provided by simulator (and not IMNC or any other Agent)
        if(tb_msg.containsKey("msg"))
        {
            Map<String, Object> msg = (Map<String, Object>)tb_msg.get("msg");
            if(msg.containsKey("source"))
            {
                String src = (String)msg.get("source");
                if(!src.equalsIgnoreCase("simulator"))
                {
                    return null;
                }
            }
        }
        else {return null;}

        if (tb_msg.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) tb_msg.get("data");
            if (data.containsKey("mission_timer")) {
                String timer_val = (String) data.get("mission_timer");
                Long count_down = parseMissionTimer(timer_val);
                if (mission_timer_clock_active == false && count_down != null && count_down > 0) {
                    mission_timer_started(timer_val, count_down);
                } else if (mission_timer_clock_active == true && (count_down == null || count_down == 0)) {
                    mission_timer_stopped(timer_val);
                }
                this.mission_timer_value = timer_val;
                if (count_down != null) {
                    long ts = this.mission_timer_clock_max_value - count_down;
                    TreeMap<String, Object> msg = this.makeClockMsgFromHeader(tb_msg);
                    if (msg != null) {
                        msg.put("mission_clock", ts);
                    }
                    return msg;
                }
            }
        }
        return null;
    }

    private void addMissionId(TreeMap<String, Object> rmq_msg, TreeMap<String, Object> data) {
        if (data.containsKey("msg")) {
            Map<String, Object> msg_obj = (Map) data.get("msg");
            if (msg_obj.containsKey("trial_id")) {
                String trialid = (String) msg_obj.get("trial_id");
                rmq_msg.put("mission-id", trialid);
            }
        }
    }

    public void publish(@NotNull String msg) {
//        System.out.println("to rmq: " + msg);
        TreeMap<String, Object> data = null;
        try {
            data = gson.fromJson(msg, TreeMap.class);
        } catch (Exception e) {
            System.out.println("Error parsing msg:\n" + msg);
            return;
        }
//        System.out.println("got: " + data);
        TreeMap<String, Object> rmq_msg = new TreeMap<String, Object>();
        String routingkey = "testbed-message";
        rmq_msg.put("timestamp", System.currentTimeMillis());
        rmq_msg.put("app-id", "TestbedBusInterface");
        rmq_msg.put("testbed-message", data);

        addMissionId(rmq_msg, data);

        TreeMap<String, Object> hdr_clock_msg = makeClockMsgFromHeader(data);
        // Not being used anymore.
//        TreeMap<String, Object> mission_clock_msg = makeClockMsgFromMissionTimer(data);

        // send clock message first so that the message is properly attributed to the clock instant and
        // not stale clock.
        if (hdr_clock_msg != null) {
            send_message("clock", hdr_clock_msg);
        }

//        if (mission_clock_msg != null) {
//            send_message("mission_clock", mission_clock_msg);
//        }

        send_message(routingkey, rmq_msg);
        if (++this.sofar % 1000 == 0) {
            System.out.println("So far sent " + this.sofar + " testbed messages to rmq");
        }
    }

    private void mission_timer_started(String timer_val, long millis_time) {
        System.out.println("Mission Timer Started: " + timer_val);
        this.mission_timer_clock_active = true;
        this.mission_timer_clock_max_value = millis_time;
    }

    private void mission_timer_stopped(String timer_val) {
        System.out.println("Mission Timer Stopped: " + this.mission_timer_value + " new_val= " + timer_val);
        this.mission_timer_clock_active = false;
    }

    private Long parseMissionTimer(String timer_val) {
        String tokens[] = timer_val.split(":");
        if (tokens != null && tokens.length >= 2) {
            Integer minutes = get_int_or_nil(tokens[0]);
            Integer seconds = get_int_or_nil(tokens[1]);
            if (minutes != null && seconds != null) {
                return new Long(1000 * (seconds + (minutes * 60)));
            }
        }
        return null;
    }

    private Integer get_int_or_nil(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
        }
        return null;
    }

    private void send_message(String routingkey, TreeMap<String, Object> msg) {
        try {
            msg.put("routing-key", routingkey);
            String json = gson.toJson(msg);
            this.rmq.publishMessage(routingkey, json);
        } catch (IOException e) {
            System.out.println("Error publishing message to rmq: " + e.getMessage() + "msg: " + msg);
//            e.printStackTrace();
        }
    }
}
