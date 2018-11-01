package data.controlers;

import pfg.config.Config;
import utils.communication.CommunicationException;
import utils.container.Service;

import java.util.ArrayList;

/**
 * Gère l'instanciation et l'iniitalisation des interfaces de communication
 *
 * @author rem
 */
public class ConnectionManager implements Service {

    /**
     * Config pour récupérer ip & port
     */
    private Config config;

    /**
     * Connections initiés
     */
    private ArrayList<Connection> initiatedConnections;

    /**
     * Pour le container
     */
    private ConnectionManager() {
        this.initiatedConnections = new ArrayList<>();
    }

    /**
     * Initialise les connections voulus
     * @param connections   les connexions à initialiser
     * @throws CommunicationException
     *                      en cas de problemes d'initialisation
     */
    public void initConnections(Connection... connections) throws CommunicationException {
        for (Connection connection : connections) {
            connection.init(this.config);
            initiatedConnections.add(connection);
        }
    }

    /**
     * Ferme toute les connections
     * @throws CommunicationException
     *                      en cas de problemes de connexion
     */
    public void closeInitiatedConnections() throws CommunicationException {
        for (Connection connection : initiatedConnections) {
            connection.close();
        }
    }

    /**
     * @return true si les connexions instanciées sont prêtes
     */
    public boolean areConnectionInitiated() {
        for (Connection connection : initiatedConnections) {
            if (!connection.isInitiated()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @see Service#updateConfig(Config)
     */
    @Override
    public void updateConfig(Config config) {
        this.config = config;
    }

    /**
     * Getter
     */
    public ArrayList<Connection> getInitiatedConnections() {
        return initiatedConnections;
    }
}
