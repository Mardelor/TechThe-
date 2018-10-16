package validation;

import data.controller.CommunicatonChannels;
import data.controller.Listener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.Container;
import utils.Log;
import utils.container.ContainerException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

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
    public void testConnection() throws Exception {
        SocketChannel slave = SocketChannel.open();
        slave.configureBlocking(true);
        slave.bind(new InetSocketAddress(6000));
        boolean state = slave.connect(new InetSocketAddress("localhost", 13500));
        Log.COMMUNICATION.debug("Slave connection state : " + state);

        String message = Arrays.toString(CommunicatonChannels.BUDDY_POSITION.getHeaders()) + "SUUS";
        ByteBuffer buffer = ByteBuffer.allocate(64);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        slave.write(buffer);

        buffer.clear();
        buffer.put((CommunicatonChannels.LIDAR + "Pouf").getBytes());
        buffer.flip();
        slave.write(buffer);

        Thread.sleep(2000);
    }
}
