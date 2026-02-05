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
    private JTextField getFilterField;
    private JTextField pinXField, pinYField;

    // this is the constructor for the BulletinBoardClient class
    public BulletinBoardClient() {
        super("Bulletin Board Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        networkClient = new NetworkClient(this);

        // top panel: where connection info is displayed
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);

        // center panel: where functionality is displayed (split into tabs for clarity)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("POST", createPostPanel());
        tabbedPane.addTab("GET", createGetPanel());
        tabbedPane.addTab("PIN/UNPIN", createPinPanel());
        tabbedPane.addTab("Actions", createActionPanel());

        // split plane to show controls and logs
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, createLogPanel());
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

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
        String[] colors = { "White", "Yellow", "Green", "Blue", "Pink" };
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

    private JPanel createGetPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton getAllButton = new JButton("GET ALL");
        // place holder -- for now, fetches simple GET ALL
        // will add more filters like color, contains, refers to later

        getAllButton.addActionListener(e -> {
            // For now just GET (ALL)
            // String cmd = CommandBuilder.buildGet(null, null);
            // networkClient.sendRequest(cmd);
        });

        panel.add(getAllButton);
        return panel;
    }

    // this private class creates the PIN panel (where you can pin notes)
    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        pinXField = new JTextField("0", 3);
        pinYField = new JTextField("0", 3);
        JButton pinButton = new JButton("PIN");
        JButton unpinButton = new JButton("UNPIN");
        JButton getPinsButton = new JButton("GET PINS");

        pinButton.addActionListener(e -> {
            try {
                int x = Integer.parseInt(pinXField.getText());
                int y = Integer.parseInt(pinYField.getText());
                networkClient.sendRequest(CommandBuilder.buildPin(x, y));
            } catch (NumberFormatException ex) {
                log("Error: Coordinates must be integers.");
            }
        });

        unpinButton.addActionListener(e -> {
            try {
                int x = Integer.parseInt(pinXField.getText());
                int y = Integer.parseInt(pinYField.getText());
                networkClient.sendRequest(CommandBuilder.buildUnpin(x, y));
            } catch (NumberFormatException ex) {
                log("Error: Coordinates must be integers.");
            }
        });

        getPinsButton.addActionListener(e -> {
            networkClient.sendRequest(CommandBuilder.buildGetPins());
        });

        panel.add(new JLabel("X:"));
        panel.add(pinXField);
        panel.add(new JLabel("Y:"));
        panel.add(pinYField);
        panel.add(pinButton);
        panel.add(unpinButton);
        panel.add(getPinsButton);

        return panel;
    }

    // this private class creates the ACTIONS panel (where you can SHAKE and CLEAR
    // the board)
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton shakeButton = new JButton("SHAKE");
        JButton clearButton = new JButton("CLEAR");

        shakeButton.addActionListener(e -> networkClient.sendRequest(CommandBuilder.buildShake()));
        clearButton.addActionListener(e -> networkClient.sendRequest(CommandBuilder.buildClear()));

        panel.add(shakeButton);
        panel.add(clearButton);
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

            String cmd = CommandBuilder.buildPost(x, y, color, message);
            networkClient.sendRequest(cmd);
        } catch (NumberFormatException e) {
            log("Error: Coordinates must be integers.");
        }
    }

    // public methods for NetworkClient to call to update the GUI
    public void onConnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            log("Connected to server.");
        });
    }

    // public methods for NetworkClient to call to update the GUI when disconnected
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            log("Disconnected from server.");
        });
    }

    // public methods for NetworkClient to call to update the GUI when logging
    // messages
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
        });
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
