package data.controller;

import java.nio.channels.SelectionKey;

/**
 * Enum permettant d'identifier les données recues et de les rediriger si besoin
 */
public enum CommunicatonChannels
{
    ROBOT_POSITION((char) 0x01, (char) 0x06),
    BUDDY_POSITION((char) 0x14, (char) 0x17),
    LIDAR((char) 0x20, (char) 0x21)
    ;

    /**
     * Identifiant du canal
     */
    private final char[] headers;

    /**
     * Clé permettant d'obtenir la socket si redirection
     */
    private SelectionKey key;

    /**
     * Construit un cannal de com
     * @param firstHeader   header 1
     * @param secondHeader  header 2
     */
    CommunicatonChannels(char firstHeader, char secondHeader) {
        headers = new char[2];
        headers[0] = firstHeader;
        headers[1] = secondHeader;
        key = null;
    }

    /**
     * Getters & Setters
     */
    public char[] getHeaders() {
        return headers;
    }
    public SelectionKey getKey() {
        return key;
    }
    public void setKey(SelectionKey key) {
        this.key = key;
    }
}
