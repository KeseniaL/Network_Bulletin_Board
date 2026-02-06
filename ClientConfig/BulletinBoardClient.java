/* This class is the main application window for the bulletin board client. 
* It is responsible for creating the GUI, handling user input, and sending requests to the server. 
* It also handles the display of the bulletin board and the notes on the board. 
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.Timer;

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
    private JTextField filterXField, filterYField, filterMsgField;
    private JComboBox<String> filterColorBox;

    // Flag to distinguish between Full Sync and Manual Filtered Query
    private boolean manualFilterActive = false;
    private boolean firstConnect = true;

    // Sync Mechanism
    private Timer pollingTimer;
    private boolean isBuffering = false;
    private boolean verboseLog = true;
    private java.util.List<ClientNote> tempNotes = new ArrayList<>();
    private java.util.List<Point> tempPins = new ArrayList<>();

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

        // control panel on the left side
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("POST", createPostPanel());
        tabbedPane.addTab("ACTIONS", createCombinedActionPanel());

        // logs panel seen on the bottom right
        JPanel logPanel = createLogPanel();

        // split control/log vertical
        JSplitPane controlLogSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, logPanel);
        controlLogSplit.setDividerLocation(400);

        // this splits the visual board vs controls
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, boardPanel, controlLogSplit);
        mainSplit.setDividerLocation(600);
        mainSplit.setResizeWeight(0.7);

        add(mainSplit, BorderLayout.CENTER);

        add(mainSplit, BorderLayout.CENTER);

        // Added an init polling timer to sync the board on connect
        pollingTimer = new Timer(3000, e -> {
            if (networkClient.isConnected()) {
                // Poll: ALWAYS fetch full board to sync state
                manualFilterActive = false;
                verboseLog = false; // Suppress logs for this poll
                networkClient.sendRequest(CommandBuilder.buildGet("", null, null, ""), true);
            }
        });

        setVisible(true);
    }

    // this private class creates the connection panel to connect to the server
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Connection"));

        // setting up the filler ip and port fields - can be changed ba
        ipField = new JTextField("localhost", 10);
        portField = new JTextField("4554", 5);
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
        panel.add(new JLabel("--- GET & FILTER ---"), gbc);

        // Filters
        // Using GridBagLayout for cleaner form
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(2, 2, 2, 2);

        filterXField = new JTextField(3);
        filterYField = new JTextField(3);
        filterMsgField = new JTextField(10);

        // Color (Dropdown)
        fgbc.gridx = 0;
        fgbc.gridy = 0;
        filterPanel.add(new JLabel("Color:"), fgbc);
        fgbc.gridx = 1;

        String[] filterColors = { "", "Yellow", "Green", "Blue", "Pink", "White" };
        filterColorBox = new JComboBox<>(filterColors);
        filterColorBox.setToolTipText("Select color to filter (Empty = All)");
        filterPanel.add(filterColorBox, fgbc);

        // Contains X Y
        fgbc.gridx = 0;
        fgbc.gridy = 1;
        filterPanel.add(new JLabel("Contains (X Y):"), fgbc);
        fgbc.gridx = 1;
        JPanel xyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filterXField.setToolTipText("X Coord");
        filterYField.setToolTipText("Y Coord");
        xyPanel.add(filterXField);
        xyPanel.add(new JLabel(" "));
        xyPanel.add(filterYField);
        filterPanel.add(xyPanel, fgbc);

        // Message
        fgbc.gridx = 0;
        fgbc.gridy = 2;
        filterPanel.add(new JLabel("Msg Text:"), fgbc);
        fgbc.gridx = 1;
        filterMsgField.setToolTipText("Filter by message content");
        filterPanel.add(filterMsgField, fgbc);

        gbc.gridy++;
        panel.add(filterPanel, gbc);

        JButton getButton = new JButton("GET");
        getButton.setToolTipText("Retrieve notes (Apply Filters if set)");
        getButton.addActionListener(e -> {
            lastCommand = "GET";
            verboseLog = true; // Manual action = show logs

            // Read UI filters
            Object selected = filterColorBox.getSelectedItem();
            String c = (selected != null) ? selected.toString().trim() : "";
            String m = filterMsgField.getText().trim();
            Integer fx = null, fy = null;
            try {
                if (!filterXField.getText().trim().isEmpty())
                    fx = Integer.parseInt(filterXField.getText().trim());
                if (!filterYField.getText().trim().isEmpty())
                    fy = Integer.parseInt(filterYField.getText().trim());
            } catch (Exception ex) {
            }

            // If filters are present, this is a Query (Don't wipe board).
            // If filters are empty, this is a Refresh (Update board).
            manualFilterActive = (!c.isEmpty() || !m.isEmpty() || fx != null || fy != null);

            networkClient.sendRequest(CommandBuilder.buildGet(c, fx, fy, m));
        });
        gbc.gridy++;
        gbc.gridy++;
        panel.add(getButton, gbc);

        // GET PINS Button
        JButton getPinsButton = new JButton("GET PINS");
        getPinsButton.setToolTipText("Retrieve only pins");
        getPinsButton.addActionListener(e -> {
            lastCommand = "GET PINS";
            verboseLog = true;
            manualFilterActive = true; // Treat as manual query to avoid clearing board (unless we want to? Users
                                       // choice usually)
                                       // Actually implementation plan said: "The pins should reappear on the empty
                                       // board"
                                       // So we won't trigger full sync buffering, just direct updates
            networkClient.sendRequest(CommandBuilder.buildGetPins());
        });
        gbc.gridy++;
        panel.add(getPinsButton, gbc);

        // --- PIN/UNPIN Section ---
        gbc.gridy++;
        panel.add(new JLabel("--- PIN / UNPIN ---"), gbc);

        // setting up the filler pin and unpin fields
        pinXField = new JTextField("0", 3);
        pinYField = new JTextField("0", 3);
        JButton pinButton = new JButton("PIN");
        JButton unpinButton = new JButton("UNPIN");

        // setting up the pin and unpin buttons to send requests to the server
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
        // setting up the unpin button to send requests to the server
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

        // setting up the pin and unpin fields
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

        // setting up the shake and clear buttons
        JButton shakeButton = new JButton("SHAKE");
        JButton clearButton = new JButton("CLEAR");

        // setting up the shake button to send requests to the server
        shakeButton.addActionListener(e -> {
            lastCommand = "SHAKE";
            networkClient.sendRequest(CommandBuilder.buildShake());
        });
        // setting up the clear button to send requests to the server
        clearButton.addActionListener(e -> {
            lastCommand = "CLEAR";
            networkClient.sendRequest(CommandBuilder.buildClear());
        });

        // setting up the shake and clear buttons in a panel
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

    // this private class creates the board configuration panel (where you can
    // configure the board)
    private void configureBoard() {
        JTextField wField = new JTextField("500", 5);
        JTextField hField = new JTextField("500", 5);
        JTextField nwField = new JTextField("50", 5);
        JTextField nhField = new JTextField("50", 5);

        // setting up the board configuration panel
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new GridLayout(4, 2));
        myPanel.add(new JLabel("Board Width:"));
        myPanel.add(wField);
        myPanel.add(new JLabel("Board Height:"));
        myPanel.add(hField);
        myPanel.add(new JLabel("Note Width:"));
        myPanel.add(nwField);
        myPanel.add(new JLabel("Note Height:"));
        myPanel.add(nhField);

        // Options for the user
        Object[] options = { "Resize Board", "Skip (Use Current)" };

        int result = JOptionPane.showOptionDialog(null, myPanel,
                "Configure Board Dimensions?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // If "Resize Board" (Index 0) is selected
        if (result == 0) {
            try {
                int w = Integer.parseInt(wField.getText());
                int h = Integer.parseInt(hField.getText());
                int nw = Integer.parseInt(nwField.getText());
                int nh = Integer.parseInt(nhField.getText());
                networkClient.sendRequest(CommandBuilder.buildResize(w, h, nw, nh));
            } catch (NumberFormatException e) {
                log("Invalid dimensions. Must be integers.");
            }
        } else {
            // "Skip" = Lock the board with current dimensions
            try {
                int w = boardPanel.getBoardWidth();
                int h = boardPanel.getBoardHeight();
                // Send explicit RESIZE to lock the config (Server won't clear if dims match)
                networkClient.sendRequest(
                        CommandBuilder.buildResize(isValid(w) ? w : 500, isValid(h) ? h : 500, noteWidth, noteHeight));
                log("Configuration confirmed. Board locked.");
            } catch (Exception e) {
                // Fallback catch-all
                networkClient.sendRequest(CommandBuilder.buildResize(500, 500, 50, 50));
            }
        }
    }

    // this private class checks if the value is valid
    private boolean isValid(int val) {
        return val > 0;
    }

    // this private class sends the POST command to the server
    private void sendPost() {
        // basic validation for POST command
        String message = postMessageField.getText();
        if (message.isEmpty()) {
            log("Message cannot be empty for POST.");
            return;
        }
        // setting up the post command to send requests to the server
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

            // checking if the note extends outside the board boundaries
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

            log("Connected to server.");

            // Handshake: Get current state to check if configured
            networkClient.sendRequest(CommandBuilder.buildGet("", null, null, ""));
        });
    }

    // public methods for NetworkClient to call to update the GUI when disconnected
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            boardPanel.clear(); // Reset board on disconnect
            log("Disconnected from server.");
            pollingTimer.stop();
        });
    }

    // public methods for NetworkClient to call to update the GUI when logging
    // messages
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            boolean isServerMsg = message.startsWith("S->C: ");
            String serverContent = isServerMsg ? message.substring(6).trim() : "";

            if (isServerMsg) {
                handleServerResponse(serverContent);
            }

            // Suppression Logic for Polling
            boolean suppress = !verboseLog && isServerMsg &&
                    (serverContent.startsWith("NOTE") ||
                            serverContent.startsWith("PIN ") ||
                            serverContent.contains("SUCCESS GET"));

            if (!suppress) {
                logArea.append(message + "\n");
            }
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

                    // Protocol: NOTE x y color message...
                    // Reconstruct message from parts[4] onwards to handle spaces correctly
                    if (parts.length > 4) {
                        msg = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
                    }

                    // Logic to show in logs
                    if (verboseLog) {
                        String logMsg = String.format("Received Note: X=%d Y=%d Color=%s Msg='%s'", x, y, color, msg);
                        log(logMsg);
                    }

                    if (isBuffering) {
                        tempNotes.add(new ClientNote(x, y, noteWidth, noteHeight, color, msg));
                    } else {
                        boardPanel.addNote(new ClientNote(x, y, noteWidth, noteHeight, color, msg));
                    }

                } catch (Exception e) {
                    log("Error parsing note line: " + response);
                }
            }
        } else if (response.contains("SUCCESS GET_COMPLETE")) {
            // Atomic Update
            if (isBuffering) {
                boardPanel.replaceAllNotes(new ArrayList<>(tempNotes), new ArrayList<>(tempPins));
                isBuffering = false;
            }
        } else if (response.contains("SUCCESS GET")) {
            // Check for dimensions in the SUCCESS GET line
            // PROTOCOL: SUCCESS GET <w> <h> <nw> <nh>
            String[] parts = response.split(" ");
            if (parts.length >= 6) { // SUCCESS, GET, w, h, nw, nh, [configured]
                try {
                    int w = Integer.parseInt(parts[2]);
                    int h = Integer.parseInt(parts[3]);
                    int nw = Integer.parseInt(parts[4]);
                    int nh = Integer.parseInt(parts[5]);
                    boardPanel.setBoardDimensions(w, h);
                    this.noteWidth = nw;
                    this.noteHeight = nh;

                    // Check logic for first connection
                    if (firstConnect) {
                        boolean isConfigured = false;
                        if (parts.length >= 7) {
                            isConfigured = Boolean.parseBoolean(parts[6]);
                        }

                        // Decide to show popup or not
                        if (!isConfigured) {
                            SwingUtilities.invokeLater(() -> configureBoard());
                        } else {
                            log("Board already configured. Skipping setup.");
                        }

                        firstConnect = false;
                        pollingTimer.start();
                    }

                } catch (Exception e) {
                }
            }

            // Only start buffering (updating board) if this is a SYNC request (not a manual
            // filter query)
            if (!manualFilterActive) {
                isBuffering = true;
                tempNotes.clear();
                tempPins.clear();
            } else {
                // If it is a manual filter query, we do NOT buffer.
                // We just let the logs show the results.
                isBuffering = false;
            }
        } else if (response.contains("SUCCESS RESIZED")) {
            boardPanel.clear();
            log("Board resized by server. Refreshing...");
            // Force a refresh if the board was resized
            networkClient.sendRequest(CommandBuilder.buildGet("", null, null, ""));
        } else if (response.contains("SUCCESS PINS EMPTY")) {
            log("No pins found on server.");
        } else if (response.startsWith("PIN ")) {
            // Logic: If strict "PIN x y" format and buffering.
            String[] parts = response.split(" ");
            if (parts.length == 3) {
                try {
                    int px = Integer.parseInt(parts[1]);
                    int py = Integer.parseInt(parts[2]);
                    if (isBuffering) {
                        tempPins.add(new Point(px, py));
                    } else {
                        boardPanel.pinNote(px, py);
                    }
                } catch (Exception e) {
                }
            }
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

    // main method to start the client
    public static void main(String[] args) {
        // use standard look and feel to keep it simple for now
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(BulletinBoardClient::new);
    }
}
