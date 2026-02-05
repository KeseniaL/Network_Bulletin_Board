import java.io.*;
import java.net.*;

/*Setting up Thread-per-client. Responsible for the following:
    - Establish one thread per client
    - Each client will have their own input/output streams
    - only one response per request
*/

// extend Thread class for Clienthnadler
public class ClientHandler extends Thread{
    private Socket socket;
    private BufferedReader in; //read text from client one at a time
    private PrintWriter out; //sends response to client, make sure sent immediately

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    //method will automatically execute 
    @Override
    public void run(){
        //create input and output stream for this client.
        try { 
            in = new BufferedReader (new InputStreamReader(socket.getInputStream())); 
            out = new PrintWriter(socket.getOutputStream(), true);
            
            //immediately send handshake info
            sendHandshake(); 

            //Dealing with client requests, server here will read request as long as its not empty and generate a response
            String line;
            while ((line = in.readLine()) != null){
                String response = ProtocolParser.parse(line); //this passes raw command to protocol parser
                out.println(response); //send single response to client once command has gone through parser

                //client has completed their request and wishes to disconnect, close socket
                if(response.equals("DISCONNECT")){
                    break;
                }
            }

        } catch (Exception e) {
            //If client abruptly/unexpectedly disconnects, this makes sure server doesn't crash and burnnn
        } finally {
            try {socket.close();} catch (IOException ignored) {}
        }
    }
    //Sends server configuration (rules and requirements) to client after connection
    private void sendHandshake(){
        out.println("BOARD " + BBoard.BOARD_WIDTH + " " + BBoard.BOARD_HEIGHT);
        out.println("NOTE " + BBoard.NOTE_WIDTH + " " + BBoard.NOTE_HEIGHT);
        out.println("COLOURS " + String.join(" ", BBoard.VALID_COLOURS));
    }
}