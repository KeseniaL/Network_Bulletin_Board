/*Responsible for the following:
    - Parsing client commands
    - double checking and validating syntax
    - success or error strings set up
*/

//class handles parsing a single client command
public class ProtocolParser {

    // Single shared board for all clients
    private static final Board board = new Board();

    public static String parse(String input) {

        if (input == null || input.isEmpty()) {
            return error("INVALID_FORMAT", "Empty command"); // rejects any empty commands immediately
        }

        String[] tokens = input.split("\\s+"); // split command by spaces, single spaces only as detailed in RFC
        String cmd = tokens[0]; // first token must be command key word

        // parsing for the different commands
        switch (cmd) {

            case "POST":
                return parsePost(tokens, input);

            case "GET":
                return parseGet(tokens);

            case "PIN":
                return parsePin(tokens);

            case "UNPIN":
                return parseUnPin(tokens);

            case "SHAKE":
            case "CLEAR":
                return ParseNoArgs(tokens, cmd);

            case "DISCONNECT":
                return "SUCCESS DISCONNECTED";

            default:
                return error("INVALID_FORMAT", "Unknown command");
        }
    }

    // this validates syntax of POST <x> <y> <colour> <message>
    private static String parsePost(String[] tokens, String fulline) {

        if (tokens.length < 5) {
            return error("INVALID_FORMAT", "POST requires x y colour message");
        }

        int x, y;
        try {
            x = Integer.parseInt(tokens[1]);
            y = Integer.parseInt(tokens[2]);
        } catch (NumberFormatException e) {
            return error("INVALID_FORMAT", "Coordinates must be non negative integers");
        }

        // validate colour
        String colour = tokens[3].toLowerCase();
        if (!BBoard.VALID_COLOURS.contains(colour)) {
            return error("COLOUR_NOT_SUPPORTED", "Colour not found in list");
        }

        // message is everything after the colour
        // Use tokens[3] (original case) to find the index, NOT the lowercased 'colour'
        // var
        int msgStart = fulline.indexOf(tokens[3]) + tokens[3].length() + 1;
        String message = "";
        if (msgStart < fulline.length()) {
            message = fulline.substring(msgStart);
        }

        return board.post(x, y, colour, message);
    }

    // validates PIN syntax: PIN <x> <y>
    private static String parsePin(String[] tokens) {

        if (tokens.length != 3) {
            return error("INVALID_FORMAT", "PIN requires x y coordinates");
        }

        int x, y;
        try {
            x = Integer.parseInt(tokens[1]);
            y = Integer.parseInt(tokens[2]);
        } catch (NumberFormatException e) {
            return error("INVALID_FORMAT", "Coordinates must be non negative integers");
        }

        return board.pin(x, y);
    }

    // validates UNPIN syntax: UNPIN <x> <y>
    private static String parseUnPin(String[] tokens) {

        if (tokens.length != 3) {
            return error("INVALID_FORMAT", "UNPIN requires x y coordinates");
        }

        int x, y;
        try {
            x = Integer.parseInt(tokens[1]);
            y = Integer.parseInt(tokens[2]);
        } catch (NumberFormatException e) {
            return error("INVALID_FORMAT", "Coordinates must be non negative integers");
        }

        return board.unpin(x, y);
    }

    // validates GET syntax and handles:
    // GET
    // GET PINS
    // GET colour=<c> contains=<x> <y> refersTo=<substring>
    private static String parseGet(String[] tokens) {

        // GET (no filters = ALL)
        if (tokens.length == 1) {
            return board.getFilteredNotes(null, null, null);
        }

        // GET PINS and configuring filetered based get
        if (tokens.length == 2 && tokens[1].equals("PINS")) {
            return board.getPins();
        }

        String colour = null;
        Integer cx = null, cy = null;
        String refersTo = null;

        for (int i = 1; i < tokens.length; i++) {

            if (tokens[i].startsWith("colour=")) {
                colour = tokens[i].substring(7).toLowerCase();
                if (!BBoard.VALID_COLOURS.contains(colour)) {
                    return error("INVALID_COLOUR", "Colour not supported");
                }

            } else if (tokens[i].startsWith("contains=")) {
                try {
                    cx = Integer.parseInt(tokens[++i]);
                    cy = Integer.parseInt(tokens[++i]);
                } catch (Exception e) {
                    return error("INVALID_COORDINATES", "Invalid contains coordinates");
                }

            } else if (tokens[i].startsWith("refersTo=")) {
                refersTo = tokens[i].substring(9);
                if (refersTo.isEmpty()) {
                    return error("INVALID_SUBSTRING", "Empty substring");
                }

            } else {
                return error("INVALID_FORMAT", "Unknown GET field");
            }
        }

        return board.getFilteredNotes(
                colour,
                (cx == null ? null : new int[] { cx, cy }),
                refersTo);
    }

    // for SHAKE and CLEAR that takes no arguments
    private static String ParseNoArgs(String[] tokens, String cmd) {

        if (tokens.length != 1) {
            return error("INVALID_FORMAT", cmd + " takes no arguments");
        }

        if (cmd.equals("SHAKE")) {
            return board.shake();
        } else {
            return board.clear();
        }
    }

    // standardizes error messages from server
    private static String error(String code, String msg) {
        return "ERROR " + code + " " + msg;
    }
}
