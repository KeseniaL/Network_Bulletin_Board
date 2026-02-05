package client;

public class ClientNote {
    int x, y, width, height;
    String color;
    String message;
    boolean isPinned;

    public ClientNote(int x, int y, int width, int height, String color, String message) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.message = message;
        this.isPinned = false;
    }

    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
