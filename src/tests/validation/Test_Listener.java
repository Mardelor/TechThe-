package validation;

import data.controller.CommunicatonChannels;
import data.controller.Listener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pfg.config.Config;
import utils.ConfigData;
import utils.Container;
import utils.container.ContainerException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Test_Listener
{
    private Listener listener;
    private Container container;

    @Before
    public void setUp() throws ContainerException {
        container = Container.getInstance("Master");
        listener = container.getService(Listener.class);
        listener.start();
    }

    @After
    public void after() {
        listener.interrupt();
        container = null;
        listener = null;
        Container.resetInstance();
    }

    @Test
    public void testSimpleConnection() throws Exception {
        SocketChannel slave = SocketChannel.open();
        slave.bind(new InetSocketAddress("localhost", 13501));
        slave.connect(new InetSocketAddress("localhost", 13500));

        sendMessageTo(slave, CommunicatonChannels.BUDDY_POSITION, "AAA");

        Thread.sleep(50);
        Assert.assertNotNull(CommunicatonChannels.BUDDY_POSITION.getChannel());
        Assert.assertEquals("AAA", listener.getBuddyPositionBuffer().poll());
    }

    @Test
    public void testRedirection() throws Exception {
        SocketChannel slave = SocketChannel.open();
        slave.bind(new InetSocketAddress("localhost", 13501));
        slave.connect(new InetSocketAddress("localhost", 13500));

        SocketChannel lowL = SocketChannel.open();
        lowL.bind(new InetSocketAddress("localhost", 13502));
        lowL.connect(new InetSocketAddress("localhost", 13500));

        sendMessageTo(slave, CommunicatonChannels.BUDDY_POSITION, "AAA");
        Thread.sleep(10);
        sendMessageTo(lowL, CommunicatonChannels.ROBOT_POSITION, "BBB");
        Thread.sleep(10);

        String response = receiveMessageFrom(slave);

        Assert.assertNotNull(CommunicatonChannels.ROBOT_POSITION.getChannel());
        Assert.assertNotNull(CommunicatonChannels.BUDDY_POSITION.getChannel());
        Assert.assertEquals("AAA", listener.getBuddyPositionBuffer().poll());
        Assert.assertEquals((CommunicatonChannels.BUDDY_POSITION.getHeaders() + 3 + "BBB"), response);
    }

    private void sendMessageTo(SocketChannel channel, CommunicatonChannels comChannel, String message) throws IOException {
        ByteBuffer[] buffers = new ByteBuffer[2];
        buffers[0] = ByteBuffer.allocate(2 + Integer.BYTES);
        buffers[1] = ByteBuffer.allocate(message.length());

        buffers[0].put(comChannel.getHeaders().getBytes());
        buffers[0].putInt(message.length());
        buffers[1].put(message.getBytes());

        buffers[0].flip();
        buffers[1].flip();

        channel.write(buffers);
    }

    private String receiveMessageFrom(SocketChannel channel) throws IOException {
        ByteBuffer[] buffers = new ByteBuffer[2];
        buffers[0] = ByteBuffer.allocate(2 + Integer.BYTES);
        byte[] h = new byte[2];
        int size;
        String headers;

        channel.read(buffers[0]);
        buffers[0].flip();
        buffers[0].get(h);
        size = buffers[0].getInt();
        headers = new String(h);

        buffers[1] = ByteBuffer.allocate(size);
        channel.read(buffers[1]);
        buffers[1].flip();
        String message = new String(buffers[1].array());

        return headers + size + message;
    }
}
