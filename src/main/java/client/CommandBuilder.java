package client;

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

    // GET (stub for now as server doesn't support filters yet)
    public static String buildGet() {
        return "GET";
    }

    // PIN <x> <y>
    public static String buildPin(int x, int y) {
        return String.format("PIN %d %d", x, y);
    }

    // UNPIN <x> <y>
    public static String buildUnpin(int x, int y) {
        return String.format("UNPIN %d %d", x, y);
    }

    // GET PINS (Server currently doesn't support this specific command based on
    // ProtocolParser, mapping to GET for now or custom if needed)
    // Looking at ProtocolParser, it only has POST, GET, PIN, UNPIN, SHAKE, CLEAR,
    // DISCONNECT
    // So we will just use GET for now to stay safe, or if the user wanted a
    // specific GET PINS we'd need server support.
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
}
