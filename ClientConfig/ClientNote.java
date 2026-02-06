/* This class is a custom swing comment (extending JPanel) and is responsible for rendering the visual data of the bulletin board, including the grid, notes, and pins.
* This is a helper class for the BulletinBoardClient class.
*/
public class ClientNote {
    int x, y, width, height;
    String color;
    String message;
    boolean isPinned;

    // constructor for the ClientNote class
    public ClientNote(int x, int y, int width, int height, String color, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.message = message;
        this.isPinned = false;
    }

    // checks if a point is inside the note
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
