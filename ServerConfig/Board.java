import java.util.*;
/*This will be the file in charge of the authorative board state.
Responsible for ensuring state-modifying operations are synchorized
*/

public class Board{

    private List<Note> notes = new ArrayList<>();

    //POST, ensuring atomicity
    public synchronized String post( int x, int y, String colour, String message){
        //Checks if note is within bounds
        if(x<0 || y < 0 || x + BBoard.NOTE_WIDTH > BBoard.BOARD_WIDTH || y + BBoard.NOTE_HEIGHT > BBoard.BOARD_HEIGHT){
            return error("OUT_OF_BOUNDS","Note out of bounds");
        }

        Note newNote = new Note( x, y, BBoard.NOTE_WIDTH, BBoard.NOTE_HEIGHT, colour, message);
        //check for note overlap
        for (Note n: notes){
            if(n.completelyOverlaps(newNote)){
                return error("COMPLETE_OVERLAP", "Note completely overlaps");
            }
        }

        notes.add(newNote);
        return "SUCCESS POST_IT_POSTED";
    }


    // Pin, ensuring atomicity
    //also same idea/ logic as post but enforcing parameters and cheking bounds
    public synchronized String pin( int x, int y){
            //Checks if pin is within bounds
            if(x<0 || y < 0 || x >= BBoard.BOARD_WIDTH|| y >= BBoard.BOARD_HEIGHT){
                return error("OUT_OF_BOUNDS","Pin out of bounds");
            }

            boolean found = false;
            //checks if a pin is in a note
            for(Note n : notes){
                if (n.contains(x, y)){
                    n.addPin(new Pin(x, y));
                    found = true;
                }
            }

            if(!found){
                return error ("NO_NOTE_AT_COORDINATE", "No note at coordinate");
            }
            return "SUCCESS PINNED";
    }   

    //Unpin, ensuring atomicity
    //a bit more straightforward than pin
    public synchronized String unpin( int x, int y){
        for (Note n : notes){
            if(n.hasPinAt(x, y)){
                n.removePinAt(x,y);
                return "SUCCESS UNPINNED";
            }
        }

        return error("PIN_NOT_FOUND", "No pin at coordinate");
    }

    //Shake, client has to see pre or post state. Also atomic opertaion.. they all are here
    public synchronized  String shake(){
        notes.removeIf(n -> !n.isPinned());
        return "SUCCESS SHAKE_COMPLETE";
    }

    //CLEAR- clears the board. same pre or post state clients will see, atomic operation
    public synchronized String clear(){
        notes.clear();
        return "SUCCESS BOARD_CLEARED";
    }
    //GET
    public synchronized String get(){
        if (notes.isEmpty()){
            return "SUCCESS GET_EMPTY";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SUCCESS GET\n");

        for (Note n : notes){
            sb.append(n.toProtocolString()).append("\n");
        }
        return sb.toString().trim(); //gets rid of very last new line
        
    }

    //standardizes error messages from server
    private static String error (String code, String msg){
            return "ERROR" + code + " " + msg;
    }
}