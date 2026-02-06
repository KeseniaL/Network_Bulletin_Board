import java.net.*;
import java.util.*;

/* Initialize Server startup and also configuring initial handshaking
* This will serve as main server class.
* Will be responsible for
*   - First setting up board requirements
*    - Parsing command arguments
*   - Receiving/accepting client connections
*   - Creating only one thread per client
*/


public class BBoard{

//This is the initial set up of board 
// Public chosen to allow further accessibility from other classes ClientHandler/ ProtocolParser (upcoming) and static chosen to be shared amongst threads
    public static int BOARD_WIDTH; //board width set
    public static int BOARD_HEIGHT; // board height set
    public static int NOTE_WIDTH; //note width set
    public static int NOTE_HEIGHT; //note height set

    public static Set<String> VALID_COLOURS = new List<>; //initializing string set for colours

// ENTRY POINT 
    public static void main(String[] args){
        /*Requires a few minimum requirements:
            - args[0] = port
            - args[1] = board width
            - args[2] = board height
            - args[3] = note width
            - args[4] = note height
        */
        if (args.length<6){
            System.err.println("Required: java BBoard <port> <board_w> <board_h> <note_w> <note_h> <colours>");
            System.exit(1);
        }
        try {
            //Parses the numeric arguments, also includes a cacth in event of invalid entry
            int port = Integer.parseInt(args[0]);

            BOARD_WIDTH = Integer.parseInt(args[1]);
            BOARD_HEIGHT = Integer.parseInt(args[2]);
            NOTE_WIDTH = Integer.parseInt(args[3]);
            NOTE_HEIGHT = Integer.parseInt(args[4]);

            //last set of arguemnts are for colours. will standardize colours to lowercase for easier comparisons
            for (int i=5; i< args.length; i++){
                VALID_COLOURS.add(args[i].toLowerCase());
            }

            //Creating a server socket and listen for clients
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Serving running on port "+ port);

            //accept loop
            while (true){
                Socket clientSocket = serverSocket.accept(); //barred until client connetcs

                //create  new thread for client- this is the handshaking starter also
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (Exception e) {
            //end server if any fatal startup error occurs
            e.printStackTrace();
        }
    }
    
}
