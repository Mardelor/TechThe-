package data;

import pfg.config.Config;
import utils.container.Service;

/**
 * Classe qui permet d'écouter toute les entrées
 */
public class Listener extends Thread implements Service
{
    /**
     * Buffers de distribution des données
     */

    /**
     * Sockets d'entrée
     */

    /**
     * Construit le listener
     */
    private Listener() {

    }

    /**
     * Boucle principale : on écoute toute les sockets et on distribue en fonction du type de donnée
     * @see Runnable#run()
     */
    @Override
    public void run() {

    }

    /**
     * @see Service#updateConfig(Config)
     */
    @Override
    public void updateConfig(Config config) {

    }
}
