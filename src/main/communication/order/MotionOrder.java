package communication.order;

import communication.Order;

/**
 * Définit les ordres de mouvement
 * @see Order
 *
 * @author rem
 */
public enum MotionOrder implements Order {
    MOVE_LENGTHWISE("d"),
    TURN("t"),
    MOVE_TO_POINT("p")
    ;

    /**
     * Chaîne de caractère à envoyer au LL correspondant au mouvement
     */
    private String stringOrder;

    /**
     * Construit un ordre
     * @param stringOrder   string correspondante
     */
    MotionOrder(String stringOrder) {
        this.stringOrder = stringOrder;
    }

    @Override
    public String getStringOrder() {
        return stringOrder;
    }
}