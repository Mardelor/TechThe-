package utils;

import pfg.config.ConfigInfo;

/**
 * Enumération contenant la liste des valeurs configurable via un fichier (config/config.txt),
 * La valeur associée dans cette classe est celle attribuée par défaut, lorsque l'on fait une faute d'orthographe
 * dans le nom de la clé par exemple.
 *
 * @author pf
 */
public enum ConfigData implements ConfigInfo
{
    /**
     * Constantes (rarement modifiées)
     */
    TABLE_X(3000),
    TABLE_Y(2000),
    TEMPS_MATCH(100),
    ETHERNET_DEFAULT_TIME(1),

    /**
     * Informations relatives au status du robot (Maître ou esclave ?)
     */
    MASTER_ROBOT_IP("192.168.0.1"),
    MASTER(true),

    /**
     * Paramètres du log
     */
    PRINT_LOG(true),
    SAVE_LOG(true),

    /**
     * Dimensions du robot
     */
    ROBOT_RAY(220),
    BUDDY_RAY(150),
    ENNEMY_RAY(220),

    /**
     * Threshold de comparaison de deux positions
     */
    VECTOR_COMPARISON_THRESHOLD(60),

    /**
     * Paramètres du Graphe
     */
    NBR_NOEUDS_X(30),
    NBR_NOEUDS_Y(20),
    NBR_NOEUDS_CIRCLE(12),
    ESPACEMENT_CIRCLE(1.2)
    ;

    /**
     * Valeur par défaut de la config (en dure)
     */
    private Object defaultValue;

    /**
     * Constructor with some default value
     *
     * @param defaultValue  valeur par défaut du paramètre
     */
    ConfigData(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Just a getter
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}
