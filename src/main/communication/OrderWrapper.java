package communication;

import communication.order.MotionOrder;
import pfg.config.Config;
import utils.ConfigData;
import utils.communication.CommunicationException;
import utils.container.Service;
import utils.maths.MathLib;
import utils.maths.Vector;

/**
 * Map des méthodes sur des ordres LL
 */
public class OrderWrapper implements Service {

    /**
     * Connection à laquelle parler
     */
    private Connection lowLevelConnection;

    /**
     * True si besoin de symetriser les ordres
     */
    private boolean symetrie;

    /**
     * Pour le container
     */
    private OrderWrapper() {}

    /**
     * Permet d'amorcer un mouvement en avant/arrière du robot
     * @param d     distance de mouvement
     */
    public void moveLengthwise(int d) {
        try {
            lowLevelConnection.send(String.format("%s %d", MotionOrder.MOVE_LENGTHWISE.getStringOrder(), d));
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet de tourner à l'angle indiqué
     * @param angle angle (en absolu)
     */
    public void turn(double angle) {
        if (symetrie) {
            angle = MathLib.modulo(Math.PI - angle, 2*Math.PI);
        }
        try {
            lowLevelConnection.send(String.format("%s %.3f", MotionOrder.TURN.getStringOrder(), angle));
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permet d'envoyer l'ordre de déplacement vers un point de la table
     * @param point le point auquel se déplacer
     */
    public void moveToPoint(Vector point) {
        if (symetrie) {
            point.setX(-point.getX());
        }
        try {
            lowLevelConnection.send(String.format("%s %d %d", MotionOrder.MOVE_TO_POINT.getStringOrder(), point.getX(), point.getY()));
        } catch (CommunicationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateConfig(Config config) {
        boolean master = config.getBoolean(ConfigData.MASTER);
        if (master) {
            this.lowLevelConnection = Connection.TEENSY_MASTER;
        } else {
            this.lowLevelConnection = Connection.TEENSY_SLAVE;
        }
        this.symetrie = config.getString(ConfigData.COULEUR).equals("violet");
    }
}
