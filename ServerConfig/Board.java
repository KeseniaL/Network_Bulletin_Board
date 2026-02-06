import java.util.*;

/*This will be the file in charge of the authorative board state.
Responsible for ensuring state-modifying operations are synchorized
*/

public class Board {

    private List<Note> notes = new ArrayList<>();

    // POST, ensuring atomicity
    public synchronized String post(int x, int y, String colour, String message) {

        // Checks if note is within bounds
        if (x < 0 || y < 0 ||
                x + BBoard.NOTE_WIDTH > BBoard.BOARD_WIDTH ||
                y + BBoard.NOTE_HEIGHT > BBoard.BOARD_HEIGHT) {

            return error("OUT_OF_BOUNDS", "Note out of bounds");
        }

        Note newNote = new Note(x, y, BBoard.NOTE_WIDTH, BBoard.NOTE_HEIGHT, colour, message);
        // check for note overlap
        for (Note n : notes) {
            if (n.x == x && n.y == y) {
                return error("COMPLETE_OVERLAP", "Note completely overlaps");
            }
        }
        notes.add(newNote);
        return "SUCCESS POST_IT_POSTED";
    }

    // Pin, ensuring atomicity
    // also same idea/ logic as post but enforcing parameters and cheking bounds
    public synchronized String pin(int x, int y) {

        // Checks if pin is within bounds
        if (x < 0 || y < 0 || x >= BBoard.BOARD_WIDTH || y >= BBoard.BOARD_HEIGHT) {
            return error("OUT_OF_BOUNDS", "Pin out of bounds");
        }

        boolean found = false;

        // checks if a pin is in a note
        for (Note n : notes) {
            if (n.contains(x, y)) {
                n.addPin(new Pin(x, y));
                found = true;
            }
        }
        // send out something saying no coordinets there
        if (!found) {
            return error("NO_NOTE_AT_COORDINATE", "No note at coordinate");
        }

        return "SUCCESS PINNED";
    }

    // Unpin, ensuring atomicity
    // a bit more straightforward than pin
    public synchronized String unpin(int x, int y) {

        for (Note n : notes) {
            if (n.hasPinAt(x, y)) {
                n.removePinAt(x, y);
                return "SUCCESS UNPINNED";
            }
        }

        return error("PIN_NOT_FOUND", "No pin at coordinate");
    }

    // Shake, client has to see pre or post state. Also atomic opertaion.. they all
    // are here
    public synchronized String shake() {
        notes.removeIf(n -> !n.isPinned());
        return "SUCCESS SHAKE_COMPLETE";
    }

    // CLEAR- clears the board. same pre or post state clients will see, atomic
    // operation
    public synchronized String clear() {
        notes.clear();
        return "SUCCESS BOARD_CLEARED";
    }

    // GET PINS
    public synchronized String getPins() {

        StringBuilder sb = new StringBuilder();
        boolean found = false;

        // gets pin at coordinates
        for (Note n : notes) {
            for (Pin p : n.getPins()) {
                sb.append("PIN ").append(p.x).append(" ").append(p.y).append("\n");
                found = true;
            }
        }

        if (!found) {
            return "SUCCESS PINS EMPTY";
        }

        return sb.toString().trim(); // gets rid of very last new line
    }

    // GET with filters (colour, contains, refersTo)
    public synchronized String getFilteredNotes(String colour, int[] contains, String refersTo) {

        // Validate 'contains' coordinates if present
        if (contains != null) {
            int cx = contains[0];
            int cy = contains[1];
            if (cx < 0 || cx >= BBoard.BOARD_WIDTH || cy < 0 || cy >= BBoard.BOARD_HEIGHT) {
                return error("OUT_OF_BOUNDS", "Invalid coordinates");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SUCCESS GET ").append(BBoard.BOARD_WIDTH).append(" ").append(BBoard.BOARD_HEIGHT).append(" ")
                .append(BBoard.NOTE_WIDTH).append(" ").append(BBoard.NOTE_HEIGHT).append(" ")
                .append(BBoard.configured).append("\n"); // Header with dimensions and configured flag
        boolean found = false;

        for (Note n : notes) {
            if (colour != null && !n.getColour().equals(colour)) {
                continue;
            }
            if (contains != null && !n.contains(contains[0], contains[1])) {
                continue;
            }
            if (refersTo != null &&
                    !n.getMessage().toLowerCase().contains(refersTo.toLowerCase())) {
                continue;
            }
            sb.append(n.toProtocolString()).append("\n");
            // Append pins for this note so client knows about them immediately
            for (Pin p : n.getPins()) {
                sb.append("PIN ").append(p.x).append(" ").append(p.y).append("\n");
            }
            found = true;
        }

        if (!found) {
            // Even if empty, send complete so client knows to clear board
            sb.append("SUCCESS GET_COMPLETE - No notes found");
            return sb.toString();
        }

        sb.append("SUCCESS GET_COMPLETE");
        return sb.toString();
    }

    // standardizes error messages from server
    private static String error(String code, String msg) {
        return "ERROR " + code + " " + msg;
    }

    public synchronized String resize(int w, int h, int nw, int nh) {
        // If dimensions match, just lock configuration (don't clear)
        if (w == BBoard.BOARD_WIDTH && h == BBoard.BOARD_HEIGHT &&
                nw == BBoard.NOTE_WIDTH && nh == BBoard.NOTE_HEIGHT) {
            BBoard.updateDimensions(w, h, nw, nh); // Sets configured = true
            return "SUCCESS RESIZED";
        }

        // Otherwise, resize and clear
        BBoard.updateDimensions(w, h, nw, nh);
        notes.clear();
        return "SUCCESS RESIZED";
    }
}
