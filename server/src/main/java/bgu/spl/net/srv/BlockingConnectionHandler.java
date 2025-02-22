package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;



    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol, int connectionId ,Connections<T> connections) {
    this.sock = sock;
    this.encdec = reader;
    this.protocol = protocol;
    this.protocol.start(connectionId, connections);
}

    @Override
    public void run() {
        try (Socket sock = this.sock) { // Automatically close the socket when done
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());          
            int read;
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read); // Decode incoming message
                if (nextMessage != null) {
                    protocol.process(nextMessage); // Process the decoded message
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // Log any exceptions
        } finally {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try {
            synchronized (out) { // Ensure thread safety during writes
                out.write(encdec.encode(msg)); // Encode and write the message
                out.flush(); // Flush the output stream to ensure the message is sent immediately
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // Log any exceptions
        }
    }
}



