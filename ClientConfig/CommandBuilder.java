/**
 * static helper class to construct RFC-compliant messages
 * will be used to ensure that there are no extra whitespace, its entered in the
 * right order and using proper termination.
 */
public class CommandBuilder {

    // POST <x> <y> <colour> <message>
    public static String buildPost(int x, int y, String color, String message) throws IllegalArgumentException {
        if (color == null || message == null) {
            throw new IllegalArgumentException("Color and message cannot be null");
        }
        return String.format("POST %d %d %s %s", x, y, color, message);
    }

    // GET (Filtered)
    public static String buildGet(String colour, Integer cx, Integer cy, String refersTo) {
        StringBuilder sb = new StringBuilder("GET");
        if (colour != null && !colour.isEmpty()) {
            sb.append(" colour=").append(colour);
        }
        if (cx != null && cy != null) {
            sb.append(" contains= ").append(cx).append(" ").append(cy);
        }
        if (refersTo != null && !refersTo.isEmpty()) {
            sb.append(" refersTo=").append(refersTo);
        }
        return sb.toString();
    }

    // PIN <x> <y>
    public static String buildPin(int x, int y) {
        return String.format("PIN %d %d", x, y);
    }

    // UNPIN <x> <y>
    public static String buildUnpin(int x, int y) {
        return String.format("UNPIN %d %d", x, y);
    }

    // GET
    public static String buildGetPins() {
        return "GET";
    }

    // SHAKE
    public static String buildShake() {
        return "SHAKE";
    }

    // CLEAR
    public static String buildClear() {
        return "CLEAR";
    }

    // RESIZE <w> <h> <nw> <nh>
    public static String buildResize(int w, int h, int nw, int nh) {
        return String.format("RESIZE %d %d %d %d", w, h, nw, nh);
    }
}
