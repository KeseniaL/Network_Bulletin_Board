package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

// makes our class a standard application window
public class BulletinBoardClient extends JFrame {

    // network client
    private NetworkClient networkClient;

    // GUI components needed
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;

    private JTextArea logArea;

    // command inputs for coordinates, message, color, filter, pin
    private JTextField postXField, postYField, postMessageField;
    private JComboBox<String> colorBox;
    // getFilterField unused for now
    private JTextField pinXField, pinYField;

    // Visual Board
    private BoardPanel boardPanel;

    // Server Params (defaults)
    private int noteWidth = 10;
    private int noteHeight = 10;

    // this is the constructor for the BulletinBoardClient class
    public BulletinBoardClient() {
        super("Bulletin Board Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Increased size for board
        setLayout(new BorderLayout());

        networkClient = new NetworkClient(this);
        boardPanel = new BoardPanel();

        // top panel: where connection info is displayed
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);

        // Control Panel (Left side)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("POST", createPostPanel());
        tabbedPane.addTab("ACTIONS", createCombinedActionPanel());

        // Logs
        JPanel logPanel = createLogPanel();

        // Split Control/Log vertical
        JSplitPane controlLogSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, logPanel);
        controlLogSplit.setDividerLocation(300);

        // Main Split: Visual Board (Center) vs Controls (East)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardPanel, controlLogSplit);
        mainSplit.setDividerLocation(600);
        mainSplit.setResizeWeight(0.7);

        add(mainSplit, BorderLayout.CENTER);

        setVisible(true);
    }

    // this private class creates the connection panel
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Connection"));

        // ip and port fields
        ipField = new JTextField("localhost", 10);
        portField = new JTextField("12345", 5);
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);

        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());

        panel.add(new JLabel("IP:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(connectButton);
        panel.add(disconnectButton);

        return panel;
    }

    // this private class creates the POST panel (where you can add notes)
    private JPanel createPostPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        postXField = new JTextField("0", 3);
        postYField = new JTextField("0", 3);
        String[] colors = { "Yellow", "Green", "Blue", "Pink", "White" };
        colorBox = new JComboBox<>(colors);
        postMessageField = new JTextField(20);

        JButton postButton = new JButton("POST Note");
        postButton.addActionListener(e -> sendPost());

        // Row 0: Coords
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X:"), gbc);
        gbc.gridx = 1;
        panel.add(postXField, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 3;
        panel.add(postYField, gbc);

        // Row 1: Color
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Color:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(colorBox, gbc);

        // Row 2: Message
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Message:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(postMessageField, gbc);

        // Row 3: Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        panel.add(postButton, gbc);

        return panel;
    }

    // this private class creates the PIN panel (where you can pin notes)
    // Unified ACTIONS panel (GET, PIN, UNPIN, SHAKE, CLEAR)
    private JPanel createCombinedActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- GET Section ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("--- GET ---"), gbc);

        JButton getButton = new JButton("GET");
        getButton.setToolTipText("Retrieve all notes from server");
        getButton.addActionListener(e -> {
            lastCommand = "GET";
            networkClient.sendRequest(CommandBuilder.buildGet());
        });
        gbc.gridy++;
        panel.add(getButton, gbc);

        // --- PIN/UNPIN Section ---
        gbc.gridy++;
        panel.add(new JLabel("--- PIN / UNPIN ---"), gbc);

        pinXField = new JTextField("0", 3);
        pinYField = new JTextField("0", 3);
        JButton pinButton = new JButton("PIN");
        JButton unpinButton = new JButton("UNPIN");

        pinButton.addActionListener(e -> {
            try {
                int x = Integer.parseInt(pinXField.getText());
                int y = Integer.parseInt(pinYField.getText());
                lastCommand = "PIN";
                networkClient.sendRequest(CommandBuilder.buildPin(x, y));
            } catch (NumberFormatException ex) {
                log("Error: Coordinates must be integers.");
            }
        });
        unpinButton.addActionListener(e -> {
            try {
                int x = Integer.parseInt(pinXField.getText());
                int y = Integer.parseInt(pinYField.getText());
                lastCommand = "UNPIN";
                networkClient.sendRequest(CommandBuilder.buildUnpin(x, y));
            } catch (NumberFormatException ex) {
                log("Error: Coordinates must be integers.");
            }
        });

        JPanel coordsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        coordsPanel.add(new JLabel("X:"));
        coordsPanel.add(pinXField);
        coordsPanel.add(new JLabel("Y:"));
        coordsPanel.add(pinYField);

        gbc.gridy++;
        panel.add(coordsPanel, gbc);

        JPanel pinBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pinBtnPanel.add(pinButton);
        pinBtnPanel.add(unpinButton);

        gbc.gridy++;
        panel.add(pinBtnPanel, gbc);

        // --- SHAKE/CLEAR Section ---
        gbc.gridy++;
        panel.add(new JLabel("--- BOARD ACTIONS ---"), gbc);

        JButton shakeButton = new JButton("SHAKE");
        JButton clearButton = new JButton("CLEAR");

        shakeButton.addActionListener(e -> {
            lastCommand = "SHAKE";
            networkClient.sendRequest(CommandBuilder.buildShake());
        });
        clearButton.addActionListener(e -> {
            lastCommand = "CLEAR";
            networkClient.sendRequest(CommandBuilder.buildClear());
        });

        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionBtnPanel.add(shakeButton);
        actionBtnPanel.add(clearButton);

        gbc.gridy++;
        panel.add(actionBtnPanel, gbc);

        // vertical spacer
        gbc.gridy++;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    // this private class creates the LOGS panel (where you can see the logs and
    // output at the bottom)
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Logs & Output"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // this private class creates the connect button (where you can connect to the
    // server)
    private void connectToServer() {
        String ip = ipField.getText();
        int port;
        try {
            port = Integer.parseInt(portField.getText());
            networkClient.connect(ip, port);
            log("Attempting connection to " + ip + ":" + port + "...");
        } catch (NumberFormatException e) {
            log("Invalid Port Number");
        }
    }

    // this private class creates the disconnect button (where you can disconnect
    // from the server)
    private void disconnectFromServer() {
        networkClient.disconnect();
    }

    private void sendPost() {
        // basic validation for POST command
        String message = postMessageField.getText();
        if (message.isEmpty()) {
            log("Message cannot be empty for POST.");
            return;
        }

        try {
            int x = Integer.parseInt(postXField.getText());
            int y = Integer.parseInt(postYField.getText());
            String color = colorBox.getSelectedItem().toString();

            if (x < 0 || y < 0) {
                log("Error: Coordinates cannot be negative.");
                return;
            }

            int bw = boardPanel.getBoardWidth();
            int bh = boardPanel.getBoardHeight();
            // Fallback for initial state if not yet set
            if (bw <= 0)
                bw = 6;
            if (bh <= 0)
                bh = 6;

            if (x + noteWidth > bw || y + noteHeight > bh) {
                log("Error: Note extends outside the board boundaries (" + bw + "x" + bh + ").");
                return;
            }

            String cmd = CommandBuilder.buildPost(x, y, color, message);

            // Overlap Check (Client-side)
            if (boardPanel.checkOverlap(x, y, noteWidth, noteHeight)) {
                log("Error: Note overlaps with an existing note!");
                return;
            }

            // Save state for optimistic update check
            lastPostNote = new ClientNote(x, y, 0, 0, color, message);
            lastPostNote.width = noteWidth;
            lastPostNote.height = noteHeight;
            lastCommand = "POST";
            networkClient.sendRequest(cmd);
        } catch (NumberFormatException e) {
            log("Error: Coordinates must be integers.");
        }
    }

    // Temp storage
    private ClientNote lastPostNote;
    private String lastCommand = "";

    // public methods for NetworkClient to call to update the GUI
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            // Reset board on new connection
            boardPanel.clear();
            log("Connected to server.");
        });
    }

    // public methods for NetworkClient to call to update the GUI when disconnected
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            boardPanel.clear(); // Reset board on disconnect
            log("Disconnected from server.");
        });
    }

    // public methods for NetworkClient to call to update the GUI when logging
    // messages
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("S->C: ")) {
                handleServerResponse(message.substring(6).trim());
            }
            logArea.append(message + "\n");
        });
    }

    // Parse server messages to update visual board
    private void handleServerResponse(String response) {
        if (response.startsWith("BOARD")) {
            // BOARD <w> <h>
            String[] parts = response.split(" ");
            if (parts.length >= 3) {
                int w = Integer.parseInt(parts[1]);
                int h = Integer.parseInt(parts[2]);
                boardPanel.setBoardDimensions(w, h);
            }
        } else if (response.startsWith("NOTE")) {
            // Handshake: NOTE <w> <h> (3 parts)
            // Data: NOTE <x> <y> <color> <message> (5+ parts)
            String[] parts = response.split(" ");
            if (parts.length == 3) {
                try {
                    noteWidth = Integer.parseInt(parts[1]);
                    noteHeight = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                }
            } else if (parts.length >= 4) {
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    String color = parts[3];
                    String msg = "";

                    // Protocol: NOTE x y color message
                    // Find start of message
                    String prefix = "NOTE " + x + " " + y + " " + color + " ";
                    if (response.length() > prefix.length()) {
                        msg = response.substring(prefix.length());
                    }

                    // Logic to show in logs
                    String logMsg = String.format("Received Note: X=%d Y=%d Color=%s Msg='%s'", x, y, color, msg);
                    log(logMsg);

                    // Add note directly (No filters)
                    boardPanel.addNote(new ClientNote(x, y, noteWidth, noteHeight, color, msg));

                } catch (Exception e) {
                    log("Error parsing note line: " + response);
                }
            }
        } else if (response.contains("SUCCESS GET")) {
            boardPanel.clear();
        } else if (response.contains("POST_PARSED") || response.contains("POST_IT_POSTED")) {
            // SUCCESS POST_PARSED
            if (lastPostNote != null) {
                lastPostNote.width = noteWidth;
                lastPostNote.height = noteHeight;
                boardPanel.addNote(lastPostNote);
                lastPostNote = null;
            }
        } else if (response.contains("PIN_PARSED") || response.contains("SUCCESS PINNED")
                || response.contains("SUCCESS UNPINNED")) {
            // SUCCESS PIN_PARSED / SUCCESS PINNED / SUCCESS UNPINNED
            // Distinguish PIN vs UNPIN
            try {
                int x = Integer.parseInt(pinXField.getText());
                int y = Integer.parseInt(pinYField.getText());

                if (response.contains("UNPINNED") || "UNPIN".equals(lastCommand)) {
                    boardPanel.unpinNote(x, y);
                } else {
                    // Default to PIN if not explicitly unpinned
                    boardPanel.pinNote(x, y);
                }
            } catch (Exception e) {
            }
        }

        else if (response.contains("SHAKE_PARSED") || response.contains("SHAKE_COMPLETE")) {
            boardPanel.shake();
        } else if (response.contains("CLEAR_PARSED") || response.contains("BOARD_CLEARED")) {
            boardPanel.clear();
        }
    }

    public static void main(String[] args) {
        // use standard look and feel to keep it simple for now
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(BulletinBoardClient::new);
    }
}
