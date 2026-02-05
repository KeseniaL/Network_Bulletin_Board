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
        return this.x == other.x && this.y == other.y && this.width == other.width && this.height == other.height;
    }
    public boolean isPinned(){
        return !pins.isEmpty();
    }
    //add pin in note, named addPin to reduce confusion with command and java class setup
    public void addPin(Pin pin){
        pins.add(pin);
    }
    /*remove/unpin pin at specific coordinate, named thsi way to prevent confusion
    public boolean Un_pin(int px, int py){
        for (Pin p :pins){
            if (p.x ==px && p.y == py){
                pins.remove(p);
                return true;
            }
        }
        return false;
    }*/
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

}