package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.srv.Connections;

public class Frame {

    private final String command; 
    private final Map<String, String> headers;
    private final String body;
    private final Connections<String> connections;
    private final int connectionId;
    private static final AtomicInteger messageCounter = new AtomicInteger(1);
    private AtomicBoolean terminate;

    public Frame(String message, Connections<String> connections, int connectionId , AtomicBoolean terminate){
        this.connections=connections;
        this.connectionId=connectionId;
        this.terminate=terminate;

        String[] parts = message.split("\n\n", 2); // Seperate between 2 parts command, headers [0] and body [1]
        String[] lines = parts[0].split("\n"); // Seperate between command [0] and headers [1]

        this.command=lines[0];
        this.body = parts.length > 1 ? parts[1] : "";

        headers=new HashMap<>();
        for(int i=1; i<lines.length; i++ ){
            String[] headerParts = lines[i].split(":", 2);
            if(headerParts.length==2)
                headers.put(headerParts[0].trim(), headerParts[1].trim());
        }
    }

    public void handleFrame(){
        switch (command) {
            case "CONNECT":
                handleConnect();
                break;
            case "SEND":
                handleSend();
                break;
            case "SUBSCRIBE":
                handleSubscribe();
                break;
            case "UNSUBSCRIBE":
                handleUnSubscribe();
                break;
            case "DISCONNECT":
                handleDisconnect();
                break;
        }
    }
        

    public void handleConnect() {
        String login = headers.get("login");
        String passcode = headers.get("passcode");
        
        if(login !=null && passcode !=null){
            String message= connections.authenticate(login ,passcode, connectionId);
            if (message!=null && !(message.equals("no error"))) { // Check if user is already logged in
                handleError(message);
                return;
            }
            connections.send(connectionId, "CONNECTED\n"+"version:1.2\n\n");
        }
    }

    public void handleSend() {
        String destination = headers.get("destination");
        if (destination != null) {
            if (connections.checkIfSubscribed(destination, connectionId)) {
                String messageId = generateMessageId();
                boolean complete = connections.send(destination, subscriberId -> { // Broadcast to all subscribers of the destination 
                    return "MESSAGE\n" +
                            "subscription:" + subscriberId + "\n" +
                            "message-id:" + messageId + "\n" +
                            "destination:" + destination + "\n\n" +body;
                });
                if (!complete) {
                    handleError("Failed to send message to destination:" + destination);
                }
                return;
            }
            handleError("Client is not subscribed to the topic:" + destination);
            return;
        }
        handleError("Failed to send message to destination:" + destination);
    }


    public void handleSubscribe() {
        String destination = headers.get("destination");
        String subscriberId = headers.get("id");
        String receipt= headers.get("receipt");

        if (destination == null || subscriberId == null) {
            handleError("Cannot successfully create the subscription");
            return;
        }
        boolean complete=connections.subscribe(destination, connectionId, subscriberId);  // Subscribe the client to the channel
        if(!complete){
            handleError("Cannot successfully create the subscription");
            return;
        }
        if (receipt != null) {
            connections.send(connectionId, "RECEIPT\n"+ "receipt-id:" + receipt + "\n\n");
        }
    }

    public void handleUnSubscribe() {
        String subscriberId = headers.get("id");
        String receipt = headers.get("receipt");
        if (subscriberId != null) {
            connections.unsubscribe(subscriberId, connectionId); // Unsubscribe the client to the channel
            if (receipt != null) {
                connections.send(connectionId, "RECEIPT\n"+"receipt-id:" + receipt + "\n\n");
            }
        }
 
    }

    public void handleDisconnect(){
        String receipt = headers.get("receipt");
        if (receipt != null) {
            connections.send(connectionId, "RECEIPT\n"+"receipt-id:" + receipt + "\n\n");
        }
        terminate.set(true);
        connections.disconnect(connectionId);

    }
    
    
    public void handleError(String errorMessage) {
        String errorFrame = "ERROR\n" +"message:" + errorMessage + "\n" +"\n"; 
        connections.send(connectionId, errorFrame);
        terminate.set(true);
        connections.disconnect(connectionId);

    }

    private String generateMessageId() {
        return String.valueOf(messageCounter.getAndIncrement());
    }
}