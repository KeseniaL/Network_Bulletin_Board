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
}

// Pin, ensuring atomicity