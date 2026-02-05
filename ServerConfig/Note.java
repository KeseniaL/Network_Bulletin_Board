import java.util.*;


//Initializing characteristics of Note for each single note on the board.

public class Note{

    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final String colour;
    public final String message;

    //next is working with pins attached to note, max of 4 pins per note

    private List<Pin> pins = new ArrayList<>();

    public Note(int x, int y, int width, int height, String colour, String message){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colour = colour;
        this.message = message;
    }
    //check if coordinates are within note
    public boolean contains(int px, int py){
        return px >= x && px < x + width &&
            py >= y && py < y + height;
    }
    //overlap detection
    public boolean completelyOverlaps(Note other){
        return other.x >= this.x && other.y >= this.y && other.x + other.width <= this.width && other.y + other.height <= this.y + this.height;
    }
    public boolean isPinned(){
        return !pins.isEmpty();
    }
    //add pin in note, named addPin to reduce confusion with command and java class setup
    public void addPin(Pin pin){
        pins.add(pin);
    }
    //return pin coordinates if pins are there 
    public boolean hasPinAt(int px, int py){
        for (Pin p: pins){
            if (p.x ==px && p.y ==py) return true;
        }
        return false;
    }
    //this will remove a pin at specific coordinate, much simpler
    public void removePinAt(int px, int py){
        pins.removeIf(p-> p.x == px && p.y == py);
    }

    //GET and subsequent portions for filter based get
    public String toProtocolString() {
        return "NOTE " + x + " " + y + " " + colour + " " + message;
    }
    //for conditional/ filter based get
    public String getColour(){
        return colour;
    }
    public String getMessage(){
        return message;
    }
    public List<Pin> getPins(){
        return pins;
    }
}