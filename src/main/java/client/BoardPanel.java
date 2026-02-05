package client;

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

    public boolean checkOverlap(int x, int y, int w, int h) {
        for (ClientNote n : notes) {
            if (x < n.x + n.width && x + w > n.x &&
                    y < n.y + n.height && y + h > n.y) {
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

        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        // Draw Grid
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= cols; i++) {
            g2d.drawLine(i * cellWidth, 0, i * cellWidth, rows * cellHeight);
        }
        for (int i = 0; i <= rows; i++) {
            g2d.drawLine(0, i * cellHeight, cols * cellWidth, i * cellHeight);
        }

        // Draw Notes
        for (ClientNote n : notes) {
            g2d.setColor(getColor(n.color));
            int nx = n.x * cellWidth;
            int ny = n.y * cellHeight;
            int nw = n.width * cellWidth;
            int nh = n.height * cellHeight;

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
            // Pin at top-left offset
            int px = p.x * cellWidth + 10;
            int py = p.y * cellHeight + 10;
            int pinSize = Math.min(cellWidth, cellHeight) / 5; // Scale pin slightly relative to cell

            g2d.fillOval(px, py, pinSize, pinSize);
            g2d.setColor(Color.WHITE); // outline makes it pop
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
