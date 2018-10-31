package utils.communication;

/**
 * Exception levée en cas de problème de communication
 *
 * @author rem
 */
public class CommunicationException extends Exception {
    CommunicationException() {super();}
    CommunicationException(String message) {super(message);}
}
