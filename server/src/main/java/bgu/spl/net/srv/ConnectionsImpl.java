package bgu.spl.net.srv;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ConnectionsImpl<T> implements Connections<T> {

    private final Map<Integer, ConnectionHandler<T>> activeClients = new ConcurrentHashMap<>(); // Maps each connectionId to its active client
    private final Map<String, Map<Integer, String>> channelSubscriptions = new ConcurrentHashMap<>(); // Maps each channel (String) to its subscribed clients connectionId (Integer) and their subscriberId (Integer)
    private final Map<String, String> users = new ConcurrentHashMap<>(); // Storage all users data (login, passcode)
    private final Map<String, Integer> loggedInUsers = new ConcurrentHashMap<>(); // Storage all loggedin users each login(String) and its own connectionId (Integer)

    public ConnectionsImpl() {}

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = activeClients.get(connectionId); // Getting ConnectionHandler that matches the given connectionId in the activeClients map
        if (handler != null) {
            handler.send(msg);
            return true;
        }
        return false;
    }

    public boolean send(String channel, Function<String, T> messageGenerator) {
        boolean complete=false;
        Map<Integer, String> subscribers = channelSubscriptions.get(channel);
        if (subscribers != null) {
            for (Map.Entry<Integer, String> entry : subscribers.entrySet()) {  // Iterate the map to get connectionId and subscriberId
                Integer connectionId = entry.getKey();
                String subscriberId = entry.getValue();
                T message = messageGenerator.apply(subscriberId);
                complete =send(connectionId, message); 
            }
        }
        return complete;
    }
    @Override
    public void disconnect(int connectionId) {
        for (Map<Integer, String> subscribers : channelSubscriptions.values()) {
            subscribers.remove(connectionId); // Remove the client from all subscribed channels
        }
        activeClients.remove(connectionId); // Remove the client from active connections
        loggedInUsers.entrySet().removeIf(entry -> entry.getValue() == connectionId); // Find and remove the user from loggedInUsers
    }

    public boolean subscribe(String channel, int connectionId, String subscriberId) {
        Map<Integer, String> subscribers = channelSubscriptions.computeIfAbsent(channel, k -> new ConcurrentHashMap<>());
        if (subscribers.containsKey(connectionId)) {
            return false;
        }
        subscribers.put(connectionId, subscriberId);
        return true; 
    }
    
    public void unsubscribe(String subscriberId, int connectionId) {
        for (Map.Entry<String, Map<Integer, String>> entry : channelSubscriptions.entrySet()) { // Iterate all channels
            Map<Integer, String> subscribers = entry.getValue();
            String channel= entry.getKey();
            boolean found=false;
            for (Map.Entry<Integer, String> subscriberEntry : subscribers.entrySet()) {
                if (connectionId== subscriberEntry.getKey() && subscriberId.equals(subscriberEntry.getValue())) { // Find the connectionId associated with the given subscriberId
                    found=true;
                    break;
                }
            }
            if (found) {
                subscribers.remove(connectionId); // If found this connectionId remove it
               if (subscribers.isEmpty()) 
                  channelSubscriptions.remove(channel); // If the channel is now empty, remove it entirely
                break; // Subscriber ID for client is unique 
            }
        }
    }

    public String authenticate(String login, String passcode, int connectionId){

        if(loggedInUsers.containsKey(login)){
            return "User already logged in";
        } else {
            users.putIfAbsent(login, passcode);
            if (!users.get(login).equals(passcode)) {
                return "Wrong Password";
            }
            
            loggedInUsers.put(login, connectionId);
            return "no error";  
        }
    }

    public void addOrUpdateConnectionHandler(int connectionId, ConnectionHandler<T> handler) {
        activeClients.put(connectionId, handler);
    }

    public boolean checkIfSubscribed(String destenation, int connectionId){
        Map<Integer, String> subscribers = channelSubscriptions.get(destenation);
        if (subscribers != null) {
            for (Map.Entry<Integer, String> entry : subscribers.entrySet()) {
                if (entry.getKey() == connectionId) {
                    return true;
                }
            }
        }
        return false;
    }
}