import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    private List<ClientNote> notes = new ArrayList<>();
    private int boardWidth = 0;
    private int boardHeight = 0;
    private List<Point> pins = new ArrayList<>();

    public BoardPanel() {
        setPreferredSize(new Dimension(600, 600)); // Default size
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    public void setBoardDimensions(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public void addNote(ClientNote note) {
        notes.add(note);
        repaint();
    }

    public void pinNote(int x, int y) {
        // Just adding a visual pin at x,y
        pins.add(new Point(x, y));
        repaint();
    }

    public void unpinNote(int x, int y) {
        // Remove pin near x,y? Or exact match?
        pins.removeIf(p -> p.x == x && p.y == y);
        repaint();
    }

    public void clear() {
        notes.clear();
        pins.clear();
        repaint();
    }

    public void replaceAllNotes(List<ClientNote> newNotes, List<Point> newPins) {
        notes.clear();
        notes.addAll(newNotes);
        pins.clear();
        pins.addAll(newPins);
        repaint();
    }

    public boolean checkOverlap(int x, int y, int w, int h) {
        for (ClientNote n : notes) {
            // Only check for exact coordinate overlap (as per Server specs)
            if (n.x == x && n.y == y) {
                return true;
            }
        }
        return false;
    }

    public void shake() {
        // Remove unpinned notes
        // We need to check which notes intersect with 'pins'.
        notes.removeIf(n -> {
            boolean hasPin = false;
            for (Point p : pins) {
                if (n.contains(p.x, p.y)) {
                    hasPin = true;
                    break;
                }
            }
            return !hasPin;
        });
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Dynamic scaling: Fill the panel
        // Use 6x6 as the default if dimensions aren't set yet
        int cols = (boardWidth > 0) ? boardWidth : 6;
        int rows = (boardHeight > 0) ? boardHeight : 6;

        // Use double precision for scaling factors to fill the space
        double cellWidth = (double) getWidth() / cols;
        double cellHeight = (double) getHeight() / rows;

        // Draw Grid
        g2d.setColor(Color.LIGHT_GRAY);
        // Optimize grid drawing: Don't draw 500 lines if cellWidth is small
        for (int i = 0; i <= cols; i++) {
            int x = (int) (i * cellWidth);
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int i = 0; i <= rows; i++) {
            int y = (int) (i * cellHeight);
            g2d.drawLine(0, y, getWidth(), y);
        }

        // Draw Notes
        for (ClientNote n : notes) {
            g2d.setColor(getColor(n.color));
            // Scale logic
            int nx = (int) (n.x * cellWidth);
            int ny = (int) (n.y * cellHeight);
            int nw = (int) Math.ceil(n.width * cellWidth);
            int nh = (int) Math.ceil(n.height * cellHeight);

            g2d.fillRect(nx, ny, nw, nh);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(nx, ny, nw, nh);

            // Draw Message
            if (n.message != null && !n.message.isEmpty()) {
                // Simple centering logic
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(n.message);

                int tx = nx + (nw - textWidth) / 2;
                int ty = ny + (nh - fm.getHeight()) / 2 + fm.getAscent();

                // Ensure text stays inside by setting clip
                Shape oldClip = g2d.getClip();
                g2d.setClip(nx, ny, nw, nh);
                g2d.drawString(n.message, tx, ty);
                g2d.setClip(oldClip);
            }
        }

        // Draw Pins
        g2d.setColor(new Color(0, 0, 139)); // Dark Blue
        for (Point p : pins) {
            // Pin at top-left offset is improper for dense grids
            // Center the pin in the "logical" cell
            int px = (int) (p.x * cellWidth + (cellWidth * 0.2));
            int py = (int) (p.y * cellHeight + (cellHeight * 0.2));

            int pinSize = (int) Math.max(5, Math.min(cellWidth, cellHeight) / 2);

            g2d.fillOval(px, py, pinSize, pinSize);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(px, py, pinSize, pinSize);
            g2d.setColor(new Color(0, 0, 139));
        }
    }

    private Color getColor(String colName) {
        switch (colName.toLowerCase()) {
            case "red":
                return new Color(255, 100, 100);
            case "blue":
                return new Color(100, 100, 255);
            case "green":
                return new Color(100, 255, 100);
            case "yellow":
                return new Color(255, 255, 200);
            case "pink":
                return Color.PINK;
            case "white":
                return Color.WHITE;
            default:
                return Color.LIGHT_GRAY;
        }
    }
}
