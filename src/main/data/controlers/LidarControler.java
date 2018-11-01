package data.controlers;

import data.Table;
import pfg.config.Config;
import utils.Log;
import utils.container.Service;
import utils.maths.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Gère les données du lidar pour modifier la table
 *
 * @author rem
 */
public class LidarControler extends Thread implements Service {

    /**
     * Temps d'attente entre deux vérification de la queue
     */
    private static final int TIME_LOOP                  = 20;

    /**
     * Separateur entre deux points
     */
    private static final String POINT_SEPARATOR         = ":";

    /**
     * Separateur entre deux coordonnées d'un point
     */
    private static final String COORDONATE_SEPARATOR    = ",";

    /**
     * Table à mettre à jour
     */
    private Table table;

    /**
     * Listener
     */
    private Listener listener;

    /**
     * File de communication avec le Listener
     */
    private ConcurrentLinkedQueue<String> messageQueue;

    /**
     * Construit un gestionnaire des données du Lidar
     * @param table     la table
     * @param listener  le listener
     */
    public LidarControler(Table table, Listener listener) {
        this.table = table;
        this.listener = listener;
        this.messageQueue = new ConcurrentLinkedQueue<>();
        listener.addQueue(Channel.LIDAR, this.messageQueue);
    }

    @Override
    public void run() {
        Log.LIDAR.debug("Controller lancé : en attente du listener...");
        while (!listener.isAlive()) {
            try {
                Thread.sleep(Listener.TIME_LOOP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.LIDAR.debug("Controller opérationnel");

        String[] points;
        ArrayList<Vector> vectors = new ArrayList<>();
        while (!Thread.currentThread().isInterrupted()) {
            while (messageQueue.peek() == null) {
                try {
                    Thread.sleep(TIME_LOOP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            points= messageQueue.poll().split(POINT_SEPARATOR);
            vectors.clear();
            for (String point : points) {
                vectors.add(new Vector(Double.parseDouble(point.split(COORDONATE_SEPARATOR)[0]),
                        Double.parseDouble(point.split(COORDONATE_SEPARATOR)[1])));
            }
            table.updateMobileObstacles(vectors);
        }
    }

    @Override
    public void updateConfig(Config config) {}
}
