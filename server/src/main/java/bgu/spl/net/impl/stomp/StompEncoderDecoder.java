package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StompEncoderDecoder implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; // Start with 1k
    private int len = 0;

    @Override
    public String decodeNextByte(byte nextByte) {

        // Check for the null character (^@), which is represented by 0x00 (0)
        if (nextByte == 0x00) {
            return popString(); // Return the complete string
        }

        pushByte(nextByte); // Otherwise, keep adding bytes
        return null; // Not done yet
    }

    @Override
    public byte[] encode(String message) {
            return (message + "\u0000").getBytes(); //uses utf8 by default
    }
    

    private void pushByte(byte nextByte) {
        // If the byte array is full, expand it
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte; // Store the byte
    }

    private String popString() {
        // Convert the byte array to a string using UTF-8 encoding
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0; // Reset the length for the next message
        return result;
    }
}
     