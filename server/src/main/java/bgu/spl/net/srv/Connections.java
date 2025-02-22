package bgu.spl.net.srv;
import java.util.function.Function;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    boolean send(String channel, Function<String, T> messageGenerator);

    void disconnect(int connectionId);


    boolean subscribe(String channel, int connectionId, String subscriberId);
    void unsubscribe(String subscriberId, int connectionId);
    String authenticate(String login, String passcode, int connectionId);
    void addOrUpdateConnectionHandler(int connectionId, ConnectionHandler<T> handler);
    boolean checkIfSubscribed(String destenation, int connectionId);

}