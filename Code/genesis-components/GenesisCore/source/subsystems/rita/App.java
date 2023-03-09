package subsystems.rita;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class App {

    @Option(names = "--host", defaultValue = "localhost", description = "RMQ Host")
    String host = "localhost";

    @Option(names = {"--port", "-p"}, defaultValue = "5672", description = "RMQ port")
    int port = 5672;

    @Option(names = {"--exchange", "-e"}, defaultValue = "rita", description = "RMQ exchange")
    String exchange = "rita";

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    public static void main(String[] args) {
        App app = new App();
        CommandLine cl = new CommandLine(app);
        cl.parseArgs(args);
        if (app.helpRequested) {
            cl.usage(System.out);
            System.exit(0);
        }
        System.out.println("Genesis Connecting to RMQ:\nHost: " +app.host + "\nPort: " + app.port + "\nExchange: " + app.exchange);

        RmqHelper rmq = RmqHelper.makeRMQ(app.host, app.port, app.exchange);
        if (rmq == null) {
            System.out.println("RMQ Connection failed");
            System.out.println("Main Done");
        } else {
            rmq.waitForMessages();
            System.out.println("Waiting for messages...\n\n");
        }
    }
}
