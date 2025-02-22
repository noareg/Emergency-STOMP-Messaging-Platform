package bgu.spl.net.impl.stomp;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;
import java.util.concurrent.atomic.AtomicBoolean;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<String> {



    private boolean shouldTerminate=false;
    private int connectionId;
    private Connections<String> connections;
    private AtomicBoolean terminate= new AtomicBoolean(false);

    @Override
    public void start(int connectionId, Connections<String> connections){
        this.connectionId=connectionId;
        this.connections=connections;
    }
    
    @Override
    public void process(String message){
       Frame frame= new Frame(message, connections, connectionId, terminate);
       frame.handleFrame();
       if (terminate.get())
        shouldTerminate = true;
    }
	
    @Override
    public boolean shouldTerminate(){
        return shouldTerminate;
    }

}