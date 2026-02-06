
/* This class handles the network connection to the server and is responsible for sending and receiving messages to and from the server. 
* It's a helper class for the BulletinBoardClient class
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// handles the network connection to the server 
public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BulletinBoardClient gui;
    private Thread listenerThread;
    private volatile boolean isRunning = false;

    // constructor in which we initialize the gui object
    public NetworkClient(BulletinBoardClient gui) {
        this.gui = gui;
    }

    // connects to the server
    public void connect(String ip, int port) {
        if (isConnected()) {
            gui.log("Already connected.");
            return;
        }
        // thread used to connect to the server by creating a socket and input/output
        // streams
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                isRunning = true;
                gui.onConnected();

                // start listener thread for server messages
                startListener();

            } catch (IOException e) {
                gui.log("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    // disconnects from the server and closes the connection
    public void disconnect() {
        if (!isConnected())
            return;

        try {
            isRunning = false;
            // send DISCONNECT to server to be polite
            sendRequest("DISCONNECT");

            // close the socket if it is not null and not closed
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) { // catch any IO exceptions
            gui.log("Error closing connection: " + e.getMessage());
        } finally {
            gui.onDisconnected();
        }
    }

    // sends a request to the server to be processed
    public void sendRequest(String request) {
        sendRequest(request, false);
    }

    public void sendRequest(String request, boolean silent) {
        if (!isConnected()) { // check if the client is connected to the server
            gui.log("Not connected to server.");
            return;
        }
        if (!silent) {
            gui.log("C->S: " + request);
        }
        out.println(request);
    }

    // starts a listener thread to receive messages from the server
    private void startListener() {
        listenerThread = new Thread(() -> {
            try { // try to read messages from the server
                String line;
                while (isRunning && (line = in.readLine()) != null) {
                    gui.log("S->C: " + line);
                }
            } catch (IOException e) { // catch any IO exceptions
                if (isRunning) {
                    gui.log("Connection lost: " + e.getMessage());
                    gui.onDisconnected();
                }
            }
        });
        listenerThread.start();
    }

    // returns true if the client is connected to the server
    public boolean isConnected() {
        return socket != null && !socket.isClosed() && isRunning;
    }
}
