package data.controller;

import pfg.config.Config;
import utils.ConfigData;
import utils.Log;
import utils.container.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Classe qui permet découter toute les entrés
 */
public class Listener extends Thread implements Service
{
    /**
     * Le selecteur
     * @see Selector
     */
    private final Selector selector;

    /**
     * Le cannal de la socket server
     */
    private final ServerSocketChannel listenChannel;

    /**
     * Numéro de port pour se connecter au HL
     */
    private final int N_PORT        = 13500;

    /**
     * Liste des clés et des sockets associées
     */
    private Map<SelectionKey, SocketChannel> map;

    /**
     * Buffer de lecture et sa taille
     */
    private ByteBuffer buffer;
    private final int BUFFER_SIZE   = 128;

    /**
     * L'IP de la Rpi du robot principale
     * Override par la config
     */
    private String mainRobotIp;

    /**
     * Status du robot : maître ou esclave ?
     */
    private boolean master;

    /**
     * Files de messages pour le traitements des données
     */
    private StringBuffer robotPositionBuffer;
    private StringBuffer buddyPositionBuffer;
    private StringBuffer lidarBuffer;

    /**
     * Construit le listener
     */
    private Listener() {
        try {
            Log.COMMUNICATION.debug("Ouverture du port...");
            selector = Selector.open();
        } catch (IOException e) {
            throw new IllegalStateException("impossible de créer le selecteur");
        }
        try {
            listenChannel = ServerSocketChannel.open();
            listenChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            listenChannel.bind(new InetSocketAddress(N_PORT));
            listenChannel.configureBlocking(false);
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
            Log.COMMUNICATION.debug("Listener prêt sur le port " + listenChannel.socket().getLocalPort());
            Log.COMMUNICATION.debug("Ouvert ! IP & Port : " + listenChannel.getLocalAddress());
        } catch (IOException e) {
            throw new IllegalStateException("impossible d'ouvrir la socket");
        }

        this.map = new HashMap<>();
        this.robotPositionBuffer = new StringBuffer();
        this.buddyPositionBuffer = new StringBuffer();
        this.lidarBuffer = new StringBuffer();
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    /**
     * Lit les headers du message, le place dans le bon buffer et le redirige si besoin
     */
    private void handleMessage(SelectionKey key) {
        char[] headers = new char[2];
        buffer.flip();
        headers[0] = buffer.getChar();
        headers[1] = buffer.getChar();

        if (Arrays.equals(headers, CommunicatonChannels.ROBOT_POSITION.getHeaders())) {
            robotPositionBuffer.append(buffer);
            if (CommunicatonChannels.ROBOT_POSITION.getKey() == null) {
                CommunicatonChannels.ROBOT_POSITION.setKey(key);
            }
            SelectionKey redirectKey = CommunicatonChannels.BUDDY_POSITION.getKey();
            if (redirectKey != null) {
                // TODO : envoie de la position avec les headers BUDDY
            }
        } else if (Arrays.equals(headers, CommunicatonChannels.BUDDY_POSITION.getHeaders())) {
            buddyPositionBuffer.append(buffer);
            Log.COMMUNICATION.debug("Message BUDDY Recu : " + buffer);
        } else if (Arrays.equals(headers, CommunicatonChannels.LIDAR.getHeaders())) {
            lidarBuffer.append(buffer);
        } else {
            Log.COMMUNICATION.warning("Headers inconnus : " + headers[0] + ", " + headers[1]);
        }
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {

        // TODO L'esclave doit se connecter au maître

        while (!Thread.currentThread().isInterrupted()) {
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            SelectionKey key;

            while (iterator.hasNext()) {
                key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    try {
                        SocketChannel newChan = listenChannel.accept();
                        if (newChan != null) {
                            newChan.configureBlocking(false);
                            SelectionKey newKey = newChan.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            map.put(newKey, newChan);
                            Log.COMMUNICATION.debug("Nouvel connection");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (key.isReadable()) {
                    try {
                        SocketChannel chan = map.get(key);
                        buffer.clear();
                        int recv = chan.read(buffer);
                        if (recv < 0) {
                            chan.close();
                            map.remove(key);
                            key.cancel();
                            Log.COMMUNICATION.critical("Connection perdue");
                        } else {
                            handleMessage(key);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @see Service#updateConfig(Config)
     */
    @Override
    public void updateConfig(Config config) {
        mainRobotIp = config.getString(ConfigData.MASTER_ROBOT_IP);
        master = config.getBoolean(ConfigData.MASTER);
    }
}
