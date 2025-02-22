package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.Server;



public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Incorrect number of arguments");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number: " + args[0]);
            return;
        }

        String serverType = args[1].toLowerCase(); // Make sure the server type is case-insensitive


        if (serverType.equals("tpc")) {
            Server.<String>threadPerClient(
                port,
                () -> {
                    StompMessagingProtocolImpl protocol = new StompMessagingProtocolImpl();
                    return protocol;
                }, 
                StompEncoderDecoder::new
                            ).serve();
        } else if (serverType.equals("reactor")) {
            Server.<String> reactor(
                Runtime.getRuntime().availableProcessors(), // Number of threads available
                port,
                () -> {
                    StompMessagingProtocolImpl protocol = new StompMessagingProtocolImpl();
                    return protocol;
                },
                StompEncoderDecoder::new // Correct Supplier for MessageEncoderDecoder
            ).serve();
        } else {
            System.out.println("Unknown server type: " + serverType);
            System.out.println("Valid options are 'reactor' or 'tpc'.");
        }
    }
}