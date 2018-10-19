package data.controller;

import utils.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Enum permettant d'identifier les données recues et de les rediriger si besoin
 */
public enum CommunicatonChannels
{
    ROBOT_POSITION((char) 0x21, (char) 0x22),
    BUDDY_POSITION((char) 0x21, (char) 0x23),
    LIDAR((char) 0x21, (char) 0x24)
    ;

    /**
     * Buffer used to send messages
     */
    private static ByteBuffer[] sendBuffers;

    /**
     * Buffer used to receive messages
     */
    private static ByteBuffer[] receivedBuffers;

    /**
     * Identifiant du canal
     */
    private final String headers;

    /**
     * Cannal Socket correspondant au canal
     */
    private SocketChannel channel;

    static {
        sendBuffers = new ByteBuffer[2];
        sendBuffers[0] = ByteBuffer.allocate(2 + Integer.BYTES);
    }

    /**
     * Construit un cannal de com
     * @param firstHeader   header 1
     * @param secondHeader  header 2
     */
    CommunicatonChannels(char firstHeader, char secondHeader) {
        char[] h = new char[2];
        h[0] = firstHeader;
        h[1] = secondHeader;
        this.headers = new String(h);
        this.channel = null;
    }

    /**
     * sends a message through a channel
     *
     * @param message   message à envoyer
     * @throws IOException
     *                  en cas de problèmes de communication
     */
    public synchronized void sendMessage(String message) throws IOException {
        if (this.channel == null) {
            Log.COMMUNICATION.warning("Unable to send messages through channel " + this.name() + " : channel null");
            return;
        }
        synchronized (sendBuffers[0]) {
            sendBuffers[0].clear();
            sendBuffers[0].put(this.headers.getBytes());
            sendBuffers[0].putInt(message.length());

            sendBuffers[1] = ByteBuffer.allocate(message.length());
            sendBuffers[1].put(message.getBytes());

            sendBuffers[0].flip();
            sendBuffers[1].flip();

            channel.write(sendBuffers);
        }
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return this.name() + " [" + headers + "]";
    }

    /**
     * Getters & Setters
     */
    public String getHeaders() {
        return headers;
    }
    public SocketChannel getChannel() {
        return channel;
    }
    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }
}
