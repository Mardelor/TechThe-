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
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Classe qui permet découter toute les entrés
 */
public class Listener extends Thread implements Service {

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
    private ByteBuffer[] buffers;
    private final int HEADER_SIZE   = 2 + Integer.BYTES;

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
    private ConcurrentLinkedQueue<String> robotPositionBuffer;
    private ConcurrentLinkedQueue<String> buddyPositionBuffer;
    private ConcurrentLinkedQueue<String> lidarBuffer;

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
        this.buffers = new ByteBuffer[2];
        this.buffers[0] = ByteBuffer.allocate(HEADER_SIZE);
        this.robotPositionBuffer = new ConcurrentLinkedQueue<>();
        this.buddyPositionBuffer = new ConcurrentLinkedQueue<>();
        this.lidarBuffer = new ConcurrentLinkedQueue<>();
    }

    /**
     * Lit les headers du message, le place dans le bon buffer et le redirige si besoin
     */
    private void handleMessage(String headers, SelectionKey key) {
        String s = new String(buffers[1].array(), Charset.forName("ASCII"));

        if (headers.equals(CommunicatonChannels.ROBOT_POSITION.getHeaders())) {
            CommunicatonChannels.ROBOT_POSITION.setChannel(map.get(key));
            robotPositionBuffer.add(s);
            try {
                CommunicatonChannels.BUDDY_POSITION.sendMessage(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (headers.equals(CommunicatonChannels.BUDDY_POSITION.getHeaders())) {
            CommunicatonChannels.BUDDY_POSITION.setChannel(map.get(key));
            buddyPositionBuffer.add(s);
        } else if (headers.equals(CommunicatonChannels.LIDAR.getHeaders())) {
            CommunicatonChannels.LIDAR.setChannel(map.get(key));
            lidarBuffer.add(s);
        } else {
            Log.COMMUNICATION.warning("Unknown headers : " + headers);
        }
    }

    /**
     * @see Thread#run()
     */
    @Override
    public void run() {

        String headers;
        byte[] h = new byte[2];
        int size;

        // TODO L'esclave doit se connecter au maître
        if (!master) {
            try {
                Log.COMMUNICATION.debug("Connection au maître...");
                SocketChannel masterChan = SocketChannel.open(new InetSocketAddress(mainRobotIp, N_PORT));
                masterChan.configureBlocking(false);
                SelectionKey masterKey = masterChan.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                map.put(masterKey, masterChan);
                CommunicatonChannels.BUDDY_POSITION.setChannel(masterChan);
                Log.COMMUNICATION.debug("Connecté !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                        buffers[0].clear();
                        long recv = chan.read(buffers[0]);

                        if (recv != 6) {
                            chan.close();
                            map.remove(key);
                            key.cancel();
                            Log.COMMUNICATION.critical("Connection perdue");
                        }

                        buffers[0].flip();
                        buffers[0].get(h, 0, h.length);
                        headers = new String(h);
                        size = buffers[0].getInt();
                        buffers[1] = ByteBuffer.allocate(size);
                        recv = chan.read(buffers[1]);

                        if (recv < 0) {
                            chan.close();
                            map.remove(key);
                            key.cancel();
                            Log.COMMUNICATION.critical("Connection perdue");
                        } else {
                            handleMessage(headers, key);
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

    /**
     * Getters & Setters
     */
    public ConcurrentLinkedQueue<String> getRobotPositionBuffer() {
        return robotPositionBuffer;
    }
    public ConcurrentLinkedQueue<String> getBuddyPositionBuffer() {
        return buddyPositionBuffer;
    }
    public ConcurrentLinkedQueue<String> getLidarBuffer() {
        return lidarBuffer;
    }
}
